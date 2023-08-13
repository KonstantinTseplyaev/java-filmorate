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
import ru.yandex.practicum.filmorate.storage.dao.impl.FilmDbStorageImpl;
import ru.yandex.practicum.filmorate.storage.dao.impl.UserDbStorageImpl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class FilmDbStorageTest {
    private final FilmDbStorageImpl filmDbStorage;
    private final UserDbStorageImpl userDbStorage;
    private Film firstFilm;
    private Film secondFilm;

    @BeforeEach
    public void createFilmsModels() {
        firstFilm = Film.builder().name("Nomadland").description("Wonderful film")
                .releaseDate(LocalDate.of(2020, 9, 11)).duration(108).mpa(new Rating(2)).build();
        firstFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        firstFilm.setLikes(new HashSet<>());
        secondFilm = Film.builder().name("Enter the void").description("Great film")
                .releaseDate(LocalDate.of(2010, 4, 29)).duration(143).mpa(new Rating(5)).build();
        secondFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        secondFilm.getGenres().add(new Genre(2));
        secondFilm.getGenres().add(new Genre(4));
        secondFilm.setLikes(new HashSet<>());
    }

    @Test
    public void findFilmById_withCorrectIdTest() {
        Film first = filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        Film film = filmDbStorage.findFilmById(first.getId());
        assertThat(film).hasFieldOrPropertyWithValue("id", film.getId())
                .hasFieldOrPropertyWithValue("name", "Nomadland");
    }

    @Test
    public void findFilmById_withIncorrectIdTest() {
        filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        assertThatThrownBy(() -> filmDbStorage.findFilmById(3)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("фильма с id " + 3 + " не существует!");
    }

    @Test
    public void createFilm_withAllPropertiesTest() {
        filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        Film film = filmDbStorage.findFilmById(2);
        assertThat(film).hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("name", "Enter the void")
                .hasFieldOrPropertyWithValue("description", "Great film")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2010, 4, 29))
                .hasFieldOrPropertyWithValue("duration", 143)
                .hasFieldOrPropertyWithValue("mpa", new Rating(5, "NC-17"))
                .hasFieldOrPropertyWithValue("genres", Set.of(new Genre(2, "Драма"), new Genre(4, "Триллер")));
    }

    @Test
    public void updateFilm_withUpdatedNameAndDescriptionTest() {
        Film film = filmDbStorage.createFilm(firstFilm);
        Film updatedFilm = Film.builder().name("Nomadland UPDATE").description("Wonderful film UPDATE")
                .releaseDate(LocalDate.of(2020, 9, 11)).duration(108).mpa(new Rating(2)).build();
        updatedFilm.setId(film.getId());
        Film ourFilm = filmDbStorage.updateFilm(updatedFilm);
        assertThat(ourFilm).hasFieldOrPropertyWithValue("id", ourFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Nomadland UPDATE")
                .hasFieldOrPropertyWithValue("description", "Wonderful film UPDATE")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2020, 9, 11))
                .hasFieldOrPropertyWithValue("duration", 108)
                .hasFieldOrPropertyWithValue("mpa", new Rating(2, "PG"))
                .hasFieldOrPropertyWithValue("genres", new TreeSet<>());
    }

    @Test
    public void updateFilm_withUpdatedGenresAndRatingTest() {
        filmDbStorage.createFilm(firstFilm);
        Film second = filmDbStorage.createFilm(secondFilm);
        Film updatedFilm = Film.builder().name("Enter the void").description("Great film")
                .releaseDate(LocalDate.of(2010, 4, 29)).duration(143).mpa(new Rating(1)).build();
        updatedFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        updatedFilm.getGenres().add(new Genre(1));
        updatedFilm.setId(second.getId());
        Film film = filmDbStorage.updateFilm(updatedFilm);
        assertThat(film).hasFieldOrPropertyWithValue("mpa", new Rating(1, "G"))
                .hasFieldOrPropertyWithValue("genres", Set.of(new Genre(1, "Комедия")))
                .hasFieldOrPropertyWithValue("name", "Enter the void");
    }

    @Test
    public void findAllFilms_whenFilmsListIsNotEmptyTest() {
        filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        List<Film> films = filmDbStorage.findAllFilms();
        assertThat(films.get(0)).hasFieldOrPropertyWithValue("name", "Nomadland");
        assertThat(films.get(1)).hasFieldOrPropertyWithValue("name", "Enter the void");
        Assertions.assertEquals(2, films.size());
    }

    @Test
    public void findAllFilms_whenFilmsListIsEmptyTest() {
        List<Film> films = filmDbStorage.findAllFilms();
        Assertions.assertTrue(films.isEmpty());
    }

    @Test
    public void deleteFilmById_withCorrectIdTest() {
        filmDbStorage.createFilm(firstFilm);
        Film second = filmDbStorage.createFilm(secondFilm);
        List<Film> films = filmDbStorage.findAllFilms();
        Assertions.assertEquals(2, films.size());
        filmDbStorage.deleteFilmById(second.getId());
        List<Film> newFilmsList = filmDbStorage.findAllFilms();
        Assertions.assertEquals(1, newFilmsList.size());
        assertThat(newFilmsList.get(0)).hasFieldOrPropertyWithValue("name", "Nomadland");
    }

    @Test
    public void deleteFilmById_withIncorrectIdTest() {
        filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        assertThatThrownBy(() -> filmDbStorage.deleteFilmById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("фильма с id " + 100 + " не существует!");
    }

    @Test
    public void deleteAllFilms_whenFilmsListAreNotEmptyTest() {
        filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        List<Film> films = filmDbStorage.findAllFilms();
        Assertions.assertEquals(2, films.size());
        filmDbStorage.deleteAllFilms();
        List<Film> actualFilms = filmDbStorage.findAllFilms();
        Assertions.assertTrue(actualFilms.isEmpty());
    }

    @Test
    public void addLike_fromNewUserTest() {
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        Film film1 = filmDbStorage.createFilm(firstFilm);
        assertThat(film1).hasFieldOrPropertyWithValue("likes", new HashSet<>());
        filmDbStorage.addLike(film1.getId(), user1.getId());
        Film film1WithLike = filmDbStorage.findFilmById(film1.getId());
        assertThat(film1WithLike).hasFieldOrPropertyWithValue("likes", Set.of(user1.getId()));
    }

    @Test
    public void addLike_fromIncorrectFilmIdTest() {
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        assertThatThrownBy(() -> filmDbStorage.addLike(1, user1.getId())).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("фильма с id " + 1 + " не существует!");
    }

    @Test
    public void addLike_duplicateTest() {
        Film film1 = filmDbStorage.createFilm(firstFilm);
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user1.getId());
        Film actualFilm = filmDbStorage.findFilmById(film1.getId());
        assertThat(actualFilm).hasFieldOrPropertyWithValue("likes", Set.of(user1.getId()));
    }

    @Test
    public void deleteLike_fromCorrectFilmIdTest() {
        Film film1 = filmDbStorage.createFilm(firstFilm);
        User user = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user.setFriendsStatuses(new HashMap<>());
        User user1 = userDbStorage.createUser(user);
        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.deleteLike(film1.getId(), user1.getId());
        Film actualFilm = filmDbStorage.findFilmById(film1.getId());
        Assertions.assertTrue(actualFilm.getLikes().isEmpty());
    }

    @Test
    public void getTopFilms_Test() {
        Film film1 = filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        Film thirdFilm = Film.builder().name("Seven").description("nice noir")
                .releaseDate(LocalDate.of(2007, 9, 11)).duration(120).mpa(new Rating(4)).build();
        thirdFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        thirdFilm.setLikes(new HashSet<>());
        Film film3 = filmDbStorage.createFilm(thirdFilm);
        User user1 = User.builder().name("Nicolas").email("myfirstemail@gmail.ru").login("myLog")
                .birthday(LocalDate.of(2000, 5, 10))
                .build();
        user1.setFriendsStatuses(new HashMap<>());
        User user2 = User.builder().name("Nataly").email("myfirstemail@gmail.ru").login("Login")
                .birthday(LocalDate.of(1989, 11, 03))
                .build();
        user2.setFriendsStatuses(new HashMap<>());
        userDbStorage.createUser(user1);
        userDbStorage.createUser(user2);
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film3.getId(), user1.getId());
        filmDbStorage.addLike(film3.getId(), user2.getId());
        List<Film> bestFilms = filmDbStorage.getTopFilms(10);
        assertThat(bestFilms.get(0)).hasFieldOrPropertyWithValue("name", "Seven");
        assertThat(bestFilms.get(1)).hasFieldOrPropertyWithValue("name", "Nomadland");
        assertThat(bestFilms.get(2)).hasFieldOrPropertyWithValue("name", "Enter the void");
    }

    @Test
    public void getAllGenres_Test() {
        List<Genre> genreList = filmDbStorage.getAllGenres();
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
        Genre genre = filmDbStorage.findGenreById(5);
        assertThat(genre).hasFieldOrPropertyWithValue("name", "Документальный");
    }

    @Test
    public void getGenreById_withIncorrectIdTest() {
        assertThatThrownBy(() -> filmDbStorage.findGenreById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("жанра с id " + 100 + " не существует!");
    }

    @Test
    public void getAllRatings_Test() {
        List<Rating> ratingList = filmDbStorage.getAllRatings();
        assertThat(ratingList.get(0)).hasFieldOrPropertyWithValue("id", 1).hasFieldOrPropertyWithValue("name", "G");
        assertThat(ratingList.get(1)).hasFieldOrPropertyWithValue("id", 2).hasFieldOrPropertyWithValue("name", "PG");
        assertThat(ratingList.get(2)).hasFieldOrPropertyWithValue("id", 3).hasFieldOrPropertyWithValue("name", "PG-13");
        assertThat(ratingList.get(3)).hasFieldOrPropertyWithValue("id", 4).hasFieldOrPropertyWithValue("name", "R");
        assertThat(ratingList.get(4)).hasFieldOrPropertyWithValue("id", 5).hasFieldOrPropertyWithValue("name", "NC-17");
        Assertions.assertEquals(5, ratingList.size());
    }

    @Test
    public void getRatingById_withCorrectIdTest() {
        Rating rating = filmDbStorage.findRatingById(2);
        assertThat(rating).hasFieldOrPropertyWithValue("name", "PG");
    }

    @Test
    public void getRatingById_withIncorrectIdTest() {
        assertThatThrownBy(() -> filmDbStorage.findRatingById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("рейтинга с id " + 100 + " не существует!");
    }
}
