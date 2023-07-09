package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film creteFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilmById(long id) {
        if (!films.containsKey(id)) {
            throw new IncorrectIdException("Фильма с таким id не существует: " + id);
        }
        films.remove(id);
    }

    @Override
    public void deleteAllFilms() {
        films.clear();
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new IncorrectIdException("Фильма с таким id не существует: " + film.getId());
        }
        Set<Long> likes = films.get(film.getId()).getLikes();
        film.setLikes(likes);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(long id) {
        if (!films.containsKey(id)) {
            throw new IncorrectIdException("Фильма с таким id не существует: " + id);
        }
        return films.get(id);
    }
}
