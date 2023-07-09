package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        log.info("запрос на добавление фильма: {}", film);
        Film newFilm = filmService.createFilm(film);
        log.info("добавлен фильм: {}", newFilm);
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(newFilm);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("запрос на обновление фильма: {}", film);
        Film updateFilm = filmService.updateFilm(film);
        log.info("обновлен фильм под id {}: {}", film.getId(), updateFilm);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateFilm);
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        log.info("запрос на получение всех фильмов");
        Collection<Film> films = filmService.getAllFilms();
        log.info("фильмы получены. Кол-во фильмов: {}", films.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable long id) {
        log.info("запрос на получение фильма по id: {}", id);
        Film film = filmService.getFilmById(id);
        log.info("получен фильм под id: {}", id);
        return ResponseEntity.ok(film);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable long id) {
        log.info("запрос на удаление фильма по id: {}", id);
        filmService.deleteFilmById(id);
        log.info("фильм под id " + id + " удален");
        return ResponseEntity.ok().body("фильм под id " + id + " удален");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteAll() {
        log.info("запрос на удаление всех фильмов");
        filmService.deleteAllFilms();
        log.info("все фильмы были удалены");
        return ResponseEntity.ok().body("все фильмы были удалены");
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<String> addLikeToFilm(@PathVariable long id, @PathVariable long userId) {
        log.info("запрос от пользователя с id {} поставить лайк фильму с id {}", userId, id);
        int likes = filmService.addLikeToFilm(id, userId);
        log.info("Лайк добавлен. Кол-во лайков у фильма под id " + id + ": " + likes);
        return ResponseEntity.ok().body("Лайк добавлен. Кол-во лайков у фильма под id " + id + ": " + likes);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<String> deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("запрос от пользователя с id {} удалить лайк с фильма под id {}", userId, id);
        int likes = filmService.deleteLike(id, userId);
        log.info("Лайк удален. Кол-во лайков у фильма под id " + id + ": " + likes);
        return ResponseEntity.ok().body("Лайк удален. Кол-во лайков у фильма под id " + id + ": " + likes);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("запрос на получение самых популярных фильмов");
        List<Film> topFilms = filmService.getTopFilms(count);
        log.info("Получен список самых популярных фильмов: {}", topFilms);
        return ResponseEntity.ok(topFilms);
    }
}
