package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.impl.FilmDbStorageImpl;

import java.util.List;

@Service
public class GenreService {
    private final FilmDbStorage filmStorage;

    @Autowired
    public GenreService(FilmDbStorageImpl filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Genre getGenreById(int genreId) {
        return filmStorage.findGenreById(genreId);
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }
}
