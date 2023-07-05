package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private int currentId = 1;

    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public ResponseEntity<?> createFilm(@Valid @RequestBody Film film) {
        try {
            if (film.getReleaseDate() != null &&
                    film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.warn("недопустимая дата релиза: {}", film.getReleaseDate());
                throw new ValidationException("недопустимая дата релиза");
            }
            film.setId(currentId++);
            films.put(film.getId(), film);
            log.info("добавлен фильм: {}", film);
            return ResponseEntity.status(201)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(film);
        } catch (ValidationException exp) {
            return ResponseEntity.status(400)
                    .body(exp.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        try {
            if (film.getReleaseDate() != null &&
                    film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.warn("недопустимая дата релиза: {}", film.getReleaseDate());
                throw new ValidationException("недопустимая дата релиза");
            }
            if (films.containsKey(film.getId())) {
                films.put(film.getId(), film);
                log.info("обновлен фильм под id {}: {}", film.getId(), film);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(film);
            } else {
                log.warn("фильма с таким id не суещствует: {}", film.getId());
                throw new ValidationException("фильма с таким id не суещствует");
            }
        } catch (ValidationException exp) {
            return ResponseEntity.status(400) //из-за того, что в случае некорректного id или даты релиза у меня в ответе пользователю отправляется месседж ошибки вместо тела фильма, не проходит несколько тестов в Postman, решил пока оставить так, думаю, это более информативно для пользователя
                    .body(exp.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        log.info("кол-во фильмов: {}", films.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(films.values());
    }

    @DeleteMapping
    public void deleteAllFilmsForTests() {
        films.clear();
        currentId = 1;
    }
}
