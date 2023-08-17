package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDbStorage {
    Film findFilmById(long id);

    long createFilm(Film film);

    void updateFilm(Film film);

    List<Film> findAllFilms();

    void deleteFilmById(long id);

    void deleteAllFilms();

    List<Film> getTopFilms(int count);
}
