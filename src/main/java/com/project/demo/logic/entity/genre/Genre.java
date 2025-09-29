package com.project.demo.logic.entity.genre;

import jakarta.persistence.*;

@Entity
@Table(name= "genres")

public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String genreName;

    private String description;

    public Genre() {

    }

    public Genre(Long id, String genreName, String description) {
        this.id = id;
        this.genreName = genreName;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
