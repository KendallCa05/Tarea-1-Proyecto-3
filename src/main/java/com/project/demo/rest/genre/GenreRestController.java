package com.project.demo.rest.genre;

import com.project.demo.logic.entity.genre.Genre;
import com.project.demo.logic.entity.genre.GenreRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.user.UserRepository;
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
@RequestMapping("/genres")
public class GenreRestController {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Genre> genresPage = genreRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(genresPage.getTotalPages());
        meta.setTotalElements(genresPage.getTotalElements());
        meta.setPageNumber(genresPage.getNumber() + 1);
        meta.setPageSize(genresPage.getSize());

        return new GlobalResponseHandler().handleResponse("Genres retrieved successfully",
                genresPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    public ResponseEntity<?> createGenre(@RequestBody Genre genre, HttpServletRequest request) {
        Genre savedGenre = genreRepository.save(genre);
        return new GlobalResponseHandler().handleResponse("Genre added successfully",
                savedGenre, HttpStatus.CREATED, request);
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<?> updateGenre(@PathVariable Long genreId, @RequestBody Genre genre, HttpServletRequest request) {
        Optional<Genre> foundGenre = genreRepository.findById(genreId);
        if(foundGenre.isPresent()) {
            genre.setId(foundGenre.get().getId());
            genreRepository.save(genre);
            return new GlobalResponseHandler().handleResponse("Genre updated successfully",
                    genre, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Genre id " + genreId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{genreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteGenre(@PathVariable Long genreId, HttpServletRequest request) {
        Optional<Genre> foundGenre = genreRepository.findById(genreId);
        if(foundGenre.isPresent()) {
            genreRepository.deleteById(foundGenre.get().getId());
            return new GlobalResponseHandler().handleResponse("Genre deleted successfully",
                    foundGenre.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Genre id " + genreId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }



}
