package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.impl.FilmDbStorageImpl;

import java.util.List;

@Service
public class RatingService {
    private final FilmDbStorage filmStorage;

    @Autowired
    public RatingService(FilmDbStorageImpl filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Rating getRatingById(int ratingId) {
        return filmStorage.findRatingById(ratingId);
    }

    public List<Rating> getAllRatings() {
        return filmStorage.getAllRatings();
    }
}
