package com.project.demo.rest.categoria;

import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.categoria.CategoriaRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/categorias")
public class CategoriaRestController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Categoria> categoriasPage = categoriaRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriasPage.getTotalPages());
        meta.setTotalElements(categoriasPage.getTotalElements());
        meta.setPageNumber(categoriasPage.getNumber() + 1);
        meta.setPageSize(categoriasPage.getSize());

        String message = categoriasPage.isEmpty() ? "No hay categorias registradas"
                : "Categorias retrieved successfully";

        return new GlobalResponseHandler().handleResponse(message,
                categoriasPage.getContent(), HttpStatus.OK, meta);
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createCategoria(@RequestBody Categoria categoria, HttpServletRequest request) {
        Categoria savedCategoria = categoriaRepository.save(categoria);
        return new GlobalResponseHandler().handleResponse("Categoria creada exitosamente",
                savedCategoria, HttpStatus.CREATED, request);
    }

    @PutMapping("/{categoriaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCategoria(@PathVariable Long categoriaId,
                                             @RequestBody Categoria categoria,
                                             HttpServletRequest request) {
        Optional<Categoria> existingCategoria = categoriaRepository.findById(categoriaId);
        if (existingCategoria.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Categoria no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }

        Categoria c = existingCategoria.get();
        c.setNombre(categoria.getNombre());
        c.setDescripcion(categoria.getDescripcion());

        categoriaRepository.save(c);

        return new GlobalResponseHandler().handleResponse("Categoria actualizada exitosamente",
                c, HttpStatus.OK, request);
    }

    @DeleteMapping("/{categoriaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCategoria(@PathVariable Long categoriaId, HttpServletRequest request) {
        Optional<Categoria> existingCategoria = categoriaRepository.findById(categoriaId);
        if (existingCategoria.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Categoria no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }

        // Validar si hay productos asociados
        if (!productoRepository.findByCategoriaId(categoriaId).isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "No se puede eliminar la categoria porque tiene productos asociados",
                    HttpStatus.BAD_REQUEST, request
            );
        }

        categoriaRepository.deleteById(categoriaId);
        return new GlobalResponseHandler().handleResponse("Categoria eliminada exitosamente",
                existingCategoria.get(), HttpStatus.OK, request);
    }
}
