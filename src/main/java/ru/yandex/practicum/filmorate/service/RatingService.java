package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.RatingDbStorage;

import java.util.List;

@Service
public class RatingService {
    private final RatingDbStorage ratingStorage;

    @Autowired
    public RatingService(RatingDbStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Rating getRatingById(int ratingId) {
        return ratingStorage.findRatingById(ratingId);
    }

    public List<Rating> getAllRatings() {
        return ratingStorage.getAllRatings();
    }
}
