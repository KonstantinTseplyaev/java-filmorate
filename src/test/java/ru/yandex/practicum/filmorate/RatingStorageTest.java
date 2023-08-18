package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.RatingStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/deleteBd.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RatingStorageTest {
    private final RatingStorage ratingStorage;

    @Test
    public void getAllRatings_Test() {
        List<Rating> ratingList = ratingStorage.getAllRatings();
        assertThat(ratingList.get(0)).hasFieldOrPropertyWithValue("id", 1).hasFieldOrPropertyWithValue("name", "G");
        assertThat(ratingList.get(1)).hasFieldOrPropertyWithValue("id", 2).hasFieldOrPropertyWithValue("name", "PG");
        assertThat(ratingList.get(2)).hasFieldOrPropertyWithValue("id", 3).hasFieldOrPropertyWithValue("name", "PG-13");
        assertThat(ratingList.get(3)).hasFieldOrPropertyWithValue("id", 4).hasFieldOrPropertyWithValue("name", "R");
        assertThat(ratingList.get(4)).hasFieldOrPropertyWithValue("id", 5).hasFieldOrPropertyWithValue("name", "NC-17");
        Assertions.assertEquals(5, ratingList.size());
    }

    @Test
    public void getRatingById_withCorrectIdTest() {
        Rating rating = ratingStorage.findRatingById(2);
        assertThat(rating).hasFieldOrPropertyWithValue("name", "PG");
    }

    @Test
    public void getRatingById_withIncorrectIdTest() {
        assertThatThrownBy(() -> ratingStorage.findRatingById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("рейтинга с id " + 100 + " не существует!");
    }
}
