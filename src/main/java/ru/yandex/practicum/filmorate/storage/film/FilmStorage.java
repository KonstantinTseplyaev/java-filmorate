package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film creteFilm(Film film);

    void deleteFilmById(long id);

    void deleteAllFilms();

    Film updateFilm(Film film);

    Collection<Film> findFilms();

    Film getFilmById(long id);
}
