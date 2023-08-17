package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorageImp;
import ru.yandex.practicum.filmorate.storage.dao.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImp;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/deleteBd.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LikeDbStorageTest {
    private final FilmDbStorageImp filmDbStorage;
    private final UserDbStorageImp userDbStorage;
    private final LikeDbStorage likeDbStorage;
    private final FilmService filmService;
    private Film firstFilm;

    @BeforeEach
    public void createFilmsModels() {
        firstFilm = Film.builder().name("Nomadland").description("Wonderful film")
                .releaseDate(LocalDate.of(2020, 9, 11)).duration(108).mpa(new Rating(2)).build();
        firstFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        firstFilm.setLikes(new HashSet<>());
    }

    @Test
    public void addLike_fromNewUserTest() {
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        long film1Id = filmDbStorage.createFilm(firstFilm);
        Film film = filmService.getFilmById(film1Id);
        assertThat(film).hasFieldOrPropertyWithValue("likes", new HashSet<>());
        likeDbStorage.addLike(film1Id, user1.getId());
        Film film1WithLike = filmService.getFilmById(film1Id);
        assertThat(film1WithLike).hasFieldOrPropertyWithValue("likes", Set.of(user1.getId()));
    }

    @Test
    public void addLike_fromIncorrectFilmIdTest() {
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        assertThatThrownBy(() -> likeDbStorage.addLike(1, user1.getId())).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("фильма с id " + 1 + " не существует!");
    }

    @Test
    public void addLike_duplicateTest() {
        long film1Id = filmDbStorage.createFilm(firstFilm);
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        likeDbStorage.addLike(film1Id, user1.getId());
        likeDbStorage.addLike(film1Id, user1.getId());
        Film actualFilm = filmService.getFilmById(film1Id);
        assertThat(actualFilm).hasFieldOrPropertyWithValue("likes", Set.of(user1.getId()));
    }

    @Test
    public void deleteLike_fromCorrectFilmIdTest() {
        long film1Id = filmDbStorage.createFilm(firstFilm);
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        likeDbStorage.addLike(film1Id, user1.getId());
        likeDbStorage.deleteLike(film1Id, user1.getId());
        Film actualFilm = filmService.getFilmById(film1Id);
        Assertions.assertTrue(actualFilm.getLikes().isEmpty());
    }
}
