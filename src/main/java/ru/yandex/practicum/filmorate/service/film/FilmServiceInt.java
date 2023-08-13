package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmServiceInt {

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    void deleteFilmById(long id);

    void deleteAllFilms();

    Film getFilmById(long id);

    int addLikeToFilm(long filmId, long userId);

    int deleteLike(long filmId, long userId);

    List<Film> getTopFilms(int count);
}
