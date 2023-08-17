package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorageImp;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImp;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FilmServiceImp implements FilmService {
    private static final LocalDate LOWER_DATE_LIMIT = LocalDate.of(1895, 12, 28);
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final LikeDbStorage likeStorage;
    private final UserDbStorage userStorage;

    @Autowired
    public FilmServiceImp(FilmDbStorageImp filmStorage, UserDbStorageImp userStorage, GenreDbStorage genreStorage,
                          LikeDbStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;
    }

    @Override
    public Film createFilm(Film film) {
        checkReleaseDate(film);
        film.setLikes(new HashSet<>());
        if (film.getGenres() == null) {
            film.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        }
        long newFilmId = filmStorage.createFilm(film);
        genreStorage.createGenres(newFilmId, film);
        likeStorage.createLikes(newFilmId, film);
        Film ourFilm = filmStorage.findFilmById(newFilmId);
        ourFilm.setGenres(genreStorage.getGenres(newFilmId));
        ourFilm.setLikes(likeStorage.getLikes(newFilmId));
        return ourFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        checkReleaseDate(film);
        Set<Long> likes = filmStorage.findFilmById(film.getId()).getLikes();
        film.setLikes(likes);
        filmStorage.updateFilm(film);
        genreStorage.deleteGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenres(film.getId(), film);
        }
        Film ourFilm = filmStorage.findFilmById(film.getId());
        ourFilm.setGenres(genreStorage.getGenres(film.getId()));
        ourFilm.setLikes(likeStorage.getLikes(film.getId()));
        return ourFilm;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = filmStorage.findAllFilms();
        for (Film film : films) {
            film.setGenres(genreStorage.getGenres(film.getId()));
            film.setLikes(likeStorage.getLikes(film.getId()));
        }
        return films;
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
        Film film = filmStorage.findFilmById(id);
        film.setGenres(genreStorage.getGenres(id));
        film.setLikes(likeStorage.getLikes(id));
        return film;
    }

    @Override
    public void addLikeToFilm(long filmId, long userId) {
        User user = userStorage.findUserById(userId);
        likeStorage.addLike(filmId, user.getId());
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        User user = userStorage.findUserById(userId);
        likeStorage.deleteLike(filmId, user.getId());
    }

    @Override
    public List<Film> getTopFilms(int count) {
        List<Film> topFilms = filmStorage.getTopFilms(count);
        for (Film film : topFilms) {
            film.setGenres(genreStorage.getGenres(film.getId()));
            film.setLikes(likeStorage.getLikes(film.getId()));
        }
        return topFilms;
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LOWER_DATE_LIMIT)) {
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
    }
}
