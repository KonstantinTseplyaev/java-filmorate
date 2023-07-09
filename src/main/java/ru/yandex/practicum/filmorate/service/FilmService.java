package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private long currentId = 0;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film createFilm(Film film) {
        film.setId(++currentId);
        film.setLikes(new HashSet<>());
        return filmStorage.creteFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findFilms();
    }

    public void deleteFilmById(long id) {
        filmStorage.deleteFilmById(id);
    }

    public void deleteAllFilms() {
        filmStorage.deleteAllFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public int addLikeToFilm(long filmId, long userId) {
        userStorage.getUserById(userId);
        Film thisFilm = filmStorage.getFilmById(filmId);
        thisFilm.getLikes().add(userId);
        return thisFilm.getLikes().size();
    }

    public int deleteLike(long filmId, long userId) {
        userStorage.getUserById(userId);
        Film thisFilm = filmStorage.getFilmById(filmId);
        if (!thisFilm.getLikes().contains(userId)) {
            throw new IncorrectIdException("пользователь не ставил лайк этому фильму");
        }
        thisFilm.getLikes().remove(userId);
        return thisFilm.getLikes().size();
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.findFilms().stream()
                .sorted((f0, f1) -> - 1 * Integer.compare(f0.getLikes().size(), f1.getLikes().size()))
                .limit(count).collect(Collectors.toList());
    }
}
