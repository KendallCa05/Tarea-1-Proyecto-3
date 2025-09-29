package com.project.demo.rest.producto;

import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.categoria.CategoriaRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoRestController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> productosPage = productoRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productosPage.getTotalPages());
        meta.setTotalElements(productosPage.getTotalElements());
        meta.setPageNumber(productosPage.getNumber() + 1);
        meta.setPageSize(productosPage.getSize());

        String message = productosPage.isEmpty() ? "No hay productos registrados"
                : "Productos retrieved successfully";

        return new GlobalResponseHandler().handleResponse(message,
                productosPage.getContent(), HttpStatus.OK, meta);
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createProducto(@RequestBody Producto producto, HttpServletRequest request) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(producto.getCategoria().getId());
        if (categoriaOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Categoria no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
        producto.setCategoria(categoriaOpt.get());
        Producto savedProducto = productoRepository.save(producto);
        return new GlobalResponseHandler().handleResponse("Producto creado exitosamente",
                savedProducto, HttpStatus.CREATED, request);
    }

    @PutMapping("/{productoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProducto(@PathVariable Long productoId,
                                            @RequestBody Producto producto,
                                            HttpServletRequest request) {
        Optional<Producto> existingProducto = productoRepository.findById(productoId);
        if (existingProducto.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Producto no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(producto.getCategoria().getId());
        if (categoriaOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Categoria no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }

        Producto p = existingProducto.get();
        p.setNombre(producto.getNombre());
        p.setDescripcion(producto.getDescripcion());
        p.setPrecio(producto.getPrecio());
        p.setCantidadStock(producto.getCantidadStock());
        p.setCategoria(categoriaOpt.get());

        productoRepository.save(p);

        return new GlobalResponseHandler().handleResponse("Producto actualizado exitosamente",
                p, HttpStatus.OK, request);
    }

    @DeleteMapping("/{productoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProducto(@PathVariable Long productoId, HttpServletRequest request) {
        Optional<Producto> existingProducto = productoRepository.findById(productoId);
        if (existingProducto.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Producto no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }
        productoRepository.deleteById(productoId);
        return new GlobalResponseHandler().handleResponse("Producto eliminado exitosamente",
                existingProducto.get(), HttpStatus.OK, request);
    }
}
