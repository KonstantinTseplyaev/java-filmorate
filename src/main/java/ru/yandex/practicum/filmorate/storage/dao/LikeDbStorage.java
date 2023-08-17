package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.Set;

@Component
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(long filmId, long userId) {
        try {
            String sql = "insert into likes(film_id, user_id) values(?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        } catch (DataIntegrityViolationException exp) {
            throw new IncorrectIdException("фильма с id " + filmId + " не существует!");
        }
    }

    public void deleteLike(long filmId, long userId) {
        jdbcTemplate.update("delete from likes where film_id = ? and user_id = ?", filmId, userId);
    }

    public Set<Long> getLikes(long filmId) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("select user_id from likes where film_id = ?", filmId);
        Set<Long> likes = new HashSet<>();
        while (likesRows.next()) {
            likes.add((long) likesRows.getInt("user_id"));
        }
        return likes;
    }

    public void createLikes(long filmId, Film film) {
        for (Long like : film.getLikes()) {
            String sqlQuery = "insert into likes(film_id, user_id) values (?, ?)";
            jdbcTemplate.update(sqlQuery, filmId, like);
        }
    }
}
