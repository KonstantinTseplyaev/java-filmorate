package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("select * from genres", (rs, rowNum) -> makeGenre(rs));
    }

    public Genre findGenreById(int id) {
        try {
            return jdbcTemplate.queryForObject("select * from genres where genre_id = ?", (rs, rowNum)
                    -> makeGenre(rs), id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("жанра с id " + id + " не существует!");
        }
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        String genreName = rs.getString("genre");
        Genre genre = new Genre(id);
        genre.setName(genreName);
        return genre;
    }

    public Set<Genre> getGenres(long filmId) {
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

    public void createGenres(long filmId, Film film) {
        if (!film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                int genreId = genre.getId();
                String sqlQuery = "insert into films_genres(film_id, genre_id) values (?, ?)";
                jdbcTemplate.update(sqlQuery, filmId, genreId);
            }
        }
    }

    public void deleteGenres(long filmId) {
        jdbcTemplate.update("delete from films_genres where film_id = ?", filmId);
    }
}
