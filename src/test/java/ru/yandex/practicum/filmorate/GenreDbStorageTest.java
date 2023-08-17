package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/deleteBd.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    @Test
    public void getAllGenres_Test() {
        List<Genre> genreList = genreDbStorage.getAllGenres();
        assertThat(genreList.get(0)).hasFieldOrPropertyWithValue("id", 1).hasFieldOrPropertyWithValue("name", "Комедия");
        assertThat(genreList.get(1)).hasFieldOrPropertyWithValue("id", 2).hasFieldOrPropertyWithValue("name", "Драма");
        assertThat(genreList.get(2)).hasFieldOrPropertyWithValue("id", 3).hasFieldOrPropertyWithValue("name", "Мультфильм");
        assertThat(genreList.get(3)).hasFieldOrPropertyWithValue("id", 4).hasFieldOrPropertyWithValue("name", "Триллер");
        assertThat(genreList.get(4)).hasFieldOrPropertyWithValue("id", 5).hasFieldOrPropertyWithValue("name", "Документальный");
        assertThat(genreList.get(5)).hasFieldOrPropertyWithValue("id", 6).hasFieldOrPropertyWithValue("name", "Боевик");
        Assertions.assertEquals(6, genreList.size());
    }

    @Test
    public void getGenreById_withCorrectIdTest() {
        Genre genre = genreDbStorage.findGenreById(5);
        assertThat(genre).hasFieldOrPropertyWithValue("name", "Документальный");
    }

    @Test
    public void getGenreById_withIncorrectIdTest() {
        assertThatThrownBy(() -> genreDbStorage.findGenreById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("жанра с id " + 100 + " не существует!");
    }
}
