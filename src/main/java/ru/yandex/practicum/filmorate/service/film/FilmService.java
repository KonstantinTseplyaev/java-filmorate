package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorageImpl;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImpl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FilmService implements FilmServiceInt {
    private static final LocalDate LOWER_DATE_LIMIT = LocalDate.of(1895, 12, 28);
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    public FilmService(FilmDbStorageImpl filmStorage, UserDbStorageImpl userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film createFilm(Film film) {
        checkReleaseDate(film);
        film.setLikes(new HashSet<>());
        if (film.getGenres() == null) {
            film.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        }
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        checkReleaseDate(film);
        Set<Long> likes = filmStorage.findFilmById(film.getId()).getLikes();
        film.setLikes(likes);
        return filmStorage.updateFilm(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.findAllFilms();
    }

    @Override
    public void deleteFilmById(long id) {
        filmStorage.deleteFilmById(id);
    }

    @Override
    public void deleteAllFilms() {
        filmStorage.deleteAllFilms();
    }

    @Override
    public Film getFilmById(long id) {
        return filmStorage.findFilmById(id);
    }

    @Override
    public void addLikeToFilm(long filmId, long userId) {
        User user = userStorage.findUserById(userId);
        filmStorage.addLike(filmId, user.getId());
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        User user = userStorage.findUserById(userId);
        filmStorage.deleteLike(filmId, user.getId());
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return filmStorage.getTopFilms(count);
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LOWER_DATE_LIMIT)) {
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
    }
}
