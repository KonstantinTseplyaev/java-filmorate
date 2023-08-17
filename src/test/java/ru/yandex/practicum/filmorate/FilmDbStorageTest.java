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
    private final FilmDbStorageImp filmDbStorage;
    private final UserDbStorageImp userDbStorage;
    private final LikeDbStorage likeDbStorage;
    private final FilmService filmService;
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
        long film1 = filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        Film film = filmDbStorage.findFilmById(film1);
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
        filmService.createFilm(firstFilm);
        filmService.createFilm(secondFilm);
        Film film = filmService.getFilmById(2);
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
        long film1 = filmDbStorage.createFilm(firstFilm);
        Film updatedFilm = Film.builder().name("Nomadland UPDATE").description("Wonderful film UPDATE")
                .releaseDate(LocalDate.of(2020, 9, 11)).duration(108).mpa(new Rating(2)).build();
        updatedFilm.setId(film1);
        filmDbStorage.updateFilm(updatedFilm);
        Film ourFilm = filmService.getFilmById(film1);
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
        long film2 = filmDbStorage.createFilm(secondFilm);
        Film updatedFilm = Film.builder().name("Enter the void").description("Great film")
                .releaseDate(LocalDate.of(2010, 4, 29)).duration(143).mpa(new Rating(1)).build();
        updatedFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        updatedFilm.getGenres().add(new Genre(1));
        updatedFilm.setId(film2);
        filmService.updateFilm(updatedFilm);
        Film ourFilm = filmService.getFilmById(film2);
        assertThat(ourFilm).hasFieldOrPropertyWithValue("mpa", new Rating(1, "G"))
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
        long secondId = filmDbStorage.createFilm(secondFilm);
        List<Film> films = filmDbStorage.findAllFilms();
        Assertions.assertEquals(2, films.size());
        filmDbStorage.deleteFilmById(secondId);
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
    public void getTopFilms_Test() {
        long film1Id = filmDbStorage.createFilm(firstFilm);
        filmDbStorage.createFilm(secondFilm);
        Film thirdFilm = Film.builder().name("Seven").description("nice noir")
                .releaseDate(LocalDate.of(2007, 9, 11)).duration(120).mpa(new Rating(4)).build();
        thirdFilm.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        thirdFilm.setLikes(new HashSet<>());
        long film3Id = filmDbStorage.createFilm(thirdFilm);
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
        likeDbStorage.addLike(film1Id, user2.getId());
        likeDbStorage.addLike(film3Id, user1.getId());
        likeDbStorage.addLike(film3Id, user2.getId());
        List<Film> bestFilms = filmDbStorage.getTopFilms(10);
        assertThat(bestFilms.get(0)).hasFieldOrPropertyWithValue("name", "Seven");
        assertThat(bestFilms.get(1)).hasFieldOrPropertyWithValue("name", "Nomadland");
        assertThat(bestFilms.get(2)).hasFieldOrPropertyWithValue("name", "Enter the void");
    }
}
