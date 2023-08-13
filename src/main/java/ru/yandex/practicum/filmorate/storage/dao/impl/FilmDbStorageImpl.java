package ru.yandex.practicum.filmorate.storage.dao.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FilmDbStorageImpl implements FilmDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    public Film createFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        long id = simpleJdbcInsert.executeAndReturnKey(toMap(film)).longValue();
        createGenres(id, film);
        createLikes(id, film);
        film.setId(id);
        film.setMpa(findRatingById(film.getMpa().getId()));
        return film;
    }

    @Override
    public Film updateFilm(Film updateFilm) {
        jdbcTemplate.update("update films set title = ?, description = ?, releasedate = ?, duration = ?, " +
                        "rating_id = ? where film_id = ?", updateFilm.getName(), updateFilm.getDescription(),
                updateFilm.getReleaseDate(), updateFilm.getDuration(), updateFilm.getMpa().getId(), updateFilm.getId());
        if (updateFilm.getGenres() != null && !updateFilm.getGenres().isEmpty()) {
            jdbcTemplate.update("delete from films_genres where film_id = ?", updateFilm.getId());
            createGenres(updateFilm.getId(), updateFilm);
        } else {
            jdbcTemplate.update("delete from films_genres where film_id = ?", updateFilm.getId());
        }
        return findFilmById(updateFilm.getId());
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
    public void addLike(long filmId, long userId) {
        try {
            String sql = "insert into likes(film_id, user_id) values(?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        } catch (DataIntegrityViolationException exp) {
            throw new IncorrectIdException("фильма с id " + filmId + " не существует!");
        }
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        jdbcTemplate.update("delete from likes where film_id = ? and user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sql = "select f.film_id, f.title, f.description, f.releasedate, f.duration, f.rating_id, " +
                "lks.sum_likes from films as f left join (select film_id, count(user_id) as sum_likes, " +
                "from likes group by film_id) as lks on f.film_id = lks.film_id order by lks.sum_likes desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("select * from genres", (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Genre findGenreById(int id) {
        try {
            return jdbcTemplate.queryForObject("select * from genres where genre_id = ?", (rs, rowNum)
                    -> makeGenre(rs), id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("жанра с id " + id + " не существует!");
        }
    }

    @Override
    public List<Rating> getAllRatings() {
        return jdbcTemplate.query("select * from rating", (rs, rowNum) -> makeRating(rs));
    }

    @Override
    public Rating findRatingById(int ratingId) {
        try {
            return jdbcTemplate.queryForObject("select * from rating where rating_id = ?", (rs, rowNum)
                    -> makeRating(rs), ratingId);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("рейтинга с id " + ratingId + " не существует!");
        }
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
        Set<Long> likes = getLikes(id);
        Set<Genre> genres = getGenres(id);
        int ratingId = rs.getInt("rating_id");
        Rating rating = findRatingById(ratingId);
        Film film = Film.builder().name(title).description(description).releaseDate(releaseDate).duration(duration)
                .mpa(rating).genres(genres).likes(likes).build();
        film.setId(id);
        return film;
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        String genreName = rs.getString("genre");
        Genre genre = new Genre(id);
        genre.setName(genreName);
        return genre;
    }

    private Rating makeRating(ResultSet rs) throws SQLException {
        int id = rs.getInt("rating_id");
        String ratingName = rs.getString("rating");
        return new Rating(id, ratingName);
    }

    private Set<Long> getLikes(long filmId) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("select user_id from likes where film_id = ?", filmId);
        Set<Long> likes = new HashSet<>();
        while (likesRows.next()) {
            likes.add((long) likesRows.getInt("user_id"));
        }
        return likes;
    }

    private Set<Genre> getGenres(long filmId) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("select * from genres where genre_id in " +
                "(select genre_id from films_genres where film_id = ?)", filmId);
        Set<Genre> strGenres = new HashSet<>();
        while (genresRows.next()) {
            Genre genreFilm = new Genre(genresRows.getInt("genre_id"),
                    genresRows.getString("genre"));
            strGenres.add(genreFilm);
        }
        return strGenres;
    }

    private void createLikes(long filmId, Film film) {
        for (Long like : film.getLikes()) {
            String sqlQuery = "insert into likes(film_id, user_id) values (?, ?)";
            jdbcTemplate.update(sqlQuery, filmId, like);
        }
    }

    private void createGenres(long filmId, Film film) {
        if (!film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                int genreId = genre.getId();
                String sqlQuery = "insert into films_genres(film_id, genre_id) values (?, ?)";
                jdbcTemplate.update(sqlQuery, filmId, genreId);
            }
        }
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
