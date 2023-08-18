package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/mpa")
public class RatingController {
    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        log.info("запрос на получение всех рейтингов");
        List<Rating> ratings = ratingService.getAllRatings();
        log.info("рейтинги получены: {}", ratings);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(ratings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable int id) {
        log.info("запрос на получение рейтинга по id {}", id);
        Rating rating = ratingService.getRatingById(id);
        log.info("рейтинг под id {} получен: {}", id, rating);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(rating);
    }
}
