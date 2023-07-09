package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("недопустимая дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
        Film newFilm = filmService.createFilm(film);
        log.info("добавлен фильм: {}", newFilm);
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(newFilm);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("недопустимая дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
        Film updateFilm = filmService.updateFilm(film);
        log.info("обновлен фильм под id {}: {}", film.getId(), updateFilm);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateFilm);
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        Collection<Film> films = filmService.getAllFilms();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable String id) {
        Film film = filmService.getFilmById(Integer.parseInt(id));
        return ResponseEntity.ok(film);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable(required = false) String id) {
        if (id != null) {
            filmService.deleteFilmById(Integer.parseInt(id));
            return ResponseEntity.ok().body("фильм под id " + id + " удален");
        } else {
            filmService.deleteAllFilms();
            return ResponseEntity.ok().body("все фильмы были удалены");
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<String> addLikeToFilm(@PathVariable String id, @PathVariable String userId) {
        int likes = filmService.addLikeToFilm(Integer.parseInt(id), Integer.parseInt(userId));
        return ResponseEntity.ok().body("Лайк добавлен. Кол-во лайков у фильма под id " + id + ": " + likes);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<String> deleteLike(@PathVariable String id, @PathVariable String userId) {
        int likes = filmService.deleteLike(Integer.parseInt(id), Integer.parseInt(userId));
        return ResponseEntity.ok().body("Лайк удален. Кол-во лайков у фильма под id " + id + ": " + likes);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getTopFilms(@RequestParam(defaultValue = "10") String count) {
        List<Film> topFilms = filmService.getTopFilms(Integer.parseInt(count));
        return ResponseEntity.ok(topFilms);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidationExpCount(final ValidationException exp) {
        return ResponseEntity.status(400).body((Map.of("error", "Ошибка при валидации", "errorMessage",
                exp.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleIncorrectIDExpCount(final IncorrectIdException exp) {
        return ResponseEntity.status(404).body((Map.of("error", "Ошибка при указании id фильма/пользователя",
                "errorMessage",
                exp.getMessage())));
    }
}
