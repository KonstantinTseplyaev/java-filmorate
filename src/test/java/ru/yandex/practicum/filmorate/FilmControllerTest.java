package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ResponseExceptionHandler;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.film.FilmServiceInt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class FilmControllerTest {
    @Autowired
    private FilmController controller;
    @Autowired
    private FilmServiceInt service;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private Film film1;
    private Film film2;

    @BeforeEach
    public void createModels() {
        film1 = Film.builder().name("Nomadland").description("Wonderful film")
                .releaseDate(LocalDate.of(2020, 9, 11)).duration(108).mpa(new Rating(2)).build();
        film2 = Film.builder().name("Enter the void").description("Wonderful film")
                .releaseDate(LocalDate.of(2010, 4, 29)).duration(143).mpa(new Rating(4)).build();
        film1.setId(1);
        film2.setId(2);
    }

    @AfterEach
    public void removeFilms() throws Exception {
        service.deleteAllFilms();
    }

    @Test
    public void createFilmWithCorrectDataTest() throws Exception {
        service.createFilm(film1);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films/" + film1.getId()));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(film1)));
    }

    @Test
    public void createFilmWithNameAndRatingTest() throws Exception {
        Film film = Film.builder().name("Melancholy").mpa(new Rating(2)).build();
        Film newFilm = service.createFilm(film);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films/" + newFilm.getId()));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(newFilm)));
    }

    @Test
    public void createFilmWithNameIsEmptyTest() throws Exception {
        Film film = Film.builder().description("niceFilm").releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .mpa(new Rating(3))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("name", "название не может быть пустым"))))));
    }

    @Test
    public void createFilmWithDescriptionsLengthIs201Test() throws Exception {
        Film film = Film.builder().name("Seven").description("x".repeat(201))
                .releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .mpa(new Rating(3))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("description", "длина описания не более 200 символов"))))));
    }

    @Test
    public void createFilmWithDescriptionsLengthIs200Test() throws Exception {
        Film film = Film.builder().name("Seven").description("x".repeat(200))
                .releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .mpa(new Rating(3))
                .build();
        service.createFilm(film);
        film.setLikes(new HashSet<>());
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films/" + film.getId()));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void createFilmWithReleaseDateIs27December1895Test() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .mpa(new Rating(3))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error", "Ошибка при валидации",
                "errorMessage", "недопустимая дата релиза: " + film.getReleaseDate()))));
    }

    @Test
    public void createFilmWithReleaseDateIs28December1895Test() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(80)
                .mpa(new Rating(3))
                .build();
        service.createFilm(film);
        film.setLikes(new HashSet<>());
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films/" + film.getId()));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void createFilmWithDurationIsZeroTest() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(0)
                .mpa(new Rating(3))
                .build();
        Film newFilm = service.createFilm(film);
        film.setLikes(new HashSet<>());
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films/" + newFilm.getId()));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(newFilm)));
    }

    @Test
    public void createFilmWithDurationIsNegativeTest() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(-1)
                .mpa(new Rating(3))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("duration", "длительность должна быть положительной"))))));
    }

    @Test
    public void getFilms_whenFilmsAreEmptyTest() throws Exception {
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films"));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json("[]"));
    }

    @Test
    public void getFilms_whenFilmsAreValidTest() throws Exception {
        Film film3 = Film.builder().name("Enter the Void").description("niceFilm").releaseDate(LocalDate.of(2019,
                        7, 7))
                .duration(100)
                .mpa(new Rating(3))
                .build();
        film3.setId(3);
        Collection<Film> filmsList = List.of(film1, film2, film3);
        service.createFilm(film1);
        service.createFilm(film2);
        service.createFilm(film3);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/films"));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilm_withCorrectDataTest() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .mpa(new Rating(3, "PG-13"))
                .genres(new TreeSet<>(Comparator.comparing(Genre::getId)))
                .build();
        film1Update.setId(film1.getId());
        film1Update.setLikes(new HashSet<>());
        Collection<Film> filmsList = List.of(film1Update, film2);
        ResultActions putResponse = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film1Update)));
        putResponse.andExpect(MockMvcResultMatchers.status().is(200));
        putResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(film1Update)));
        ResultActions getResponse = mockMvc.perform(MockMvcRequestBuilders.get("/films"));
        getResponse.andExpect(MockMvcResultMatchers.status().is(200));
        getResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilmWithNotValidDataTest() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().description("niceFilmUp").releaseDate(LocalDate.of(2000, 9, 22))
                .duration(85)
                .mpa(new Rating(3))
                .build();
        film1Update.setId(1);
        Film film2Update = Film.builder().name("Seven").description("x".repeat(201))
                .releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .mpa(new Rating(4))
                .build();
        film2Update.setId(1);
        Film film3Update = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .mpa(new Rating(1))
                .build();
        film3Update.setId(1);
        Film film4Update = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(-1)
                .mpa(new Rating(3))
                .build();
        film4Update.setId(1);
        Collection<Film> filmsList = List.of(film1, film2);
        ResultActions response1 = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film1Update)));
        response1.andExpect(MockMvcResultMatchers.status().is(400));
        response1.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("name", "название не может быть пустым"))))));
        ResultActions response2 = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film2Update)));
        response2.andExpect(MockMvcResultMatchers.status().is(400));
        response2.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("description", "длина описания не более 200 символов"))))));
        ResultActions response3 = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film3Update)));
        response3.andExpect(MockMvcResultMatchers.status().is(400));
        response3.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error", "Ошибка при валидации",
                "errorMessage", "недопустимая дата релиза: " + film3Update.getReleaseDate()))));
        ResultActions response4 = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film4Update)));
        response4.andExpect(MockMvcResultMatchers.status().is(400));
        response4.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("duration", "длительность должна быть положительной"))))));
        ResultActions getResponse = mockMvc.perform(MockMvcRequestBuilders.get("/films"));
        getResponse.andExpect(MockMvcResultMatchers.status().is(200));
        getResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilm_withIncorrectIdTest() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .mpa(new Rating(3))
                .build();
        film1Update.setId(8);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film1Update)));
        response.andExpect(MockMvcResultMatchers.status().is(404));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error",
                "Ошибка при указании id фильма/пользователя", "errorMessage", "фильма с id " + film1Update.getId() + " не существует!"))));
    }

    private void addFilmsForUpdate() throws Exception {
        service.createFilm(film1);
        service.createFilm(film2);
    }
}

