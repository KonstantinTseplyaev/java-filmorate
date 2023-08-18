package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class RatingStorage {
    private final JdbcTemplate jdbcTemplate;

    public RatingStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Rating> getAllRatings() {
        return jdbcTemplate.query("select * from rating", (rs, rowNum) -> makeRating(rs));
    }

    public Rating findRatingById(int ratingId) {
        try {
            return jdbcTemplate.queryForObject("select * from rating where rating_id = ?", (rs, rowNum)
                    -> makeRating(rs), ratingId);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("рейтинга с id " + ratingId + " не существует!");
        }
    }

    private Rating makeRating(ResultSet rs) throws SQLException {
        int id = rs.getInt("rating_id");
        String ratingName = rs.getString("rating");
        return new Rating(id, ratingName);
    }
}
