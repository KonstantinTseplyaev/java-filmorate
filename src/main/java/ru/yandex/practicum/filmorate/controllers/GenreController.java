package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        log.info("запрос на получение всех жанров");
        List<Genre> genres = genreService.getAllGenres();
        log.info("жанры получены: {}", genres);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable int id) {
        log.info("запрос на получение жанра по id {}", id);
        Genre genre = genreService.getGenreById(id);
        log.info("жанр под id {} получен: {}", id, genre);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(genre);
    }

}
