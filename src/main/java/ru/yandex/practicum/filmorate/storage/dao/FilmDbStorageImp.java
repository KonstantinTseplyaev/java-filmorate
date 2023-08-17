package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FilmDbStorageImp implements FilmDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingDbStorage ratingDbStorage;

    public FilmDbStorageImp(JdbcTemplate jdbcTemplate, RatingDbStorage ratingDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingDbStorage = ratingDbStorage;
    }

    @Override
    public long createFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        return simpleJdbcInsert.executeAndReturnKey(toMap(film)).longValue();
    }

    @Override
    public Film findFilmById(long id) {
        try {
            String sql = "select * from films where film_id = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("фильма с id " + id + " не существует!");
        }
    }

    @Override
    public void updateFilm(Film updateFilm) {
        jdbcTemplate.update("update films set title = ?, description = ?, releasedate = ?, duration = ?, " +
                        "rating_id = ? where film_id = ?", updateFilm.getName(), updateFilm.getDescription(),
                updateFilm.getReleaseDate(), updateFilm.getDuration(), updateFilm.getMpa().getId(), updateFilm.getId());
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public void deleteFilmById(long id) {
        try {
            findFilmById(id);
            jdbcTemplate.update("delete from films where film_id = ?", id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("фильма с id " + id + " не существует!");
        }
    }

    @Override
    public void deleteAllFilms() {
        jdbcTemplate.update("delete from films");
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sql = "select f.film_id, f.title, f.description, f.releasedate, f.duration, f.rating_id, " +
                "lks.sum_likes from films as f left join (select film_id, count(user_id) as sum_likes, " +
                "from likes group by film_id) as lks on f.film_id = lks.film_id order by lks.sum_likes desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        long id = rs.getInt("film_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        LocalDate releaseDate = null;
        if (rs.getDate("releasedate") != null) {
            releaseDate = rs.getDate("releasedate").toLocalDate();
        }
        Integer duration = (Integer) rs.getObject("duration");
        int ratingId = rs.getInt("rating_id");
        Rating rating = ratingDbStorage.findRatingById(ratingId);
        Film film = Film.builder().name(title).description(description).releaseDate(releaseDate).duration(duration)
                .mpa(rating).build();
        film.setId(id);
        return film;
    }

    public Map<String, Object> toMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("title", film.getName());
        values.put("description", film.getDescription());
        values.put("releasedate", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("rating_id", film.getMpa().getId());
        return values;
    }
}
