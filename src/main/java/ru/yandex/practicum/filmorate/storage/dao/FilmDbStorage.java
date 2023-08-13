package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

public interface FilmDbStorage {
    Film findFilmById(long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<Film> findAllFilms();

    void deleteFilmById(long id);

    void deleteAllFilms();

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    List<Film> getTopFilms(int count);

    List<Genre> getAllGenres();

    Genre findGenreById(int id);

    List<Rating> getAllRatings();

    Rating findRatingById(int id);
}
