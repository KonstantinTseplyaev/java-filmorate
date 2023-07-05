package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private Film film1 = Film.builder().name("Seven").description("niceFilm")
            .releaseDate(LocalDate.of(1995, 9, 22))
            .duration(80)
            .id(1)
            .build();
    private Film film2 = Film.builder().name("Melancholy").description("niceFilm")
            .releaseDate(LocalDate.of(2010, 7, 22))
            .duration(90)
            .id(2)
            .build();

    @AfterEach
    public void removeFilms() throws Exception {
        mockMvc.perform(delete("/films"));
    }

    @Test
    public void postFilms_whenFilmAttributesAreNotEmptyAndCorrect() throws Exception {
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(film1)));
    }

    @Test
    public void postFilms_whenFilmHasOnlyName() throws Exception {
        Film film = Film.builder().name("Melancholy").id(1).build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void postFilms_whenNameIsEmpty() throws Exception {
        Film film = Film.builder().description("niceFilm").releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void postFilms_whenDescriptionsLengthIs201() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilmаывацуцтщацущаощкуьмзцдулкузщлпппппппппппппппп" +
                        "ппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппп" +
                        "пппппппппппппппппппппппппппппппппппппппппппппппппппппппп").
                releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void postFilms_whenDescriptionsLengthIs200() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilmаывацуцтщацущаощкуьмзцдулкузщлпппппппппппппппп" +
                        "пппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппп" +
                        "пппппппппппппппппппппппппппппппппппппппппппппппппппппппп").
                releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201));
    }

    @Test
    public void postFilms_whenReleaseDateIs27December1895() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").
                releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().bytes("недопустимая дата релиза".getBytes()));
    }

    @Test
    public void postFilms_whenReleaseDateIs28December1895() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").
                releaseDate(LocalDate.of(1895, 12, 28))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201));
    }

    @Test
    public void postFilms_whenDurationIsZero() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(0)
                .id(1)
                .build();
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void postFilms_whenDurationIsNegative() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(-1)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getFilms_whenFilmsAreEmpty() throws Exception {
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("[]".getBytes()));
    }

    @Test
    public void getFilms_whenWereAddedValidFilms() throws Exception {
        Film film3 = Film.builder().name("Enter the Void").description("niceFilm").releaseDate(LocalDate.of(2019,
                        7, 7))
                .duration(100)
                .id(3)
                .build();
        Collection<Film> films = List.of(film1, film2, film3);
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film3))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(films)));
    }

    @Test
    public void getFilms_whenWereAddedNotValidFilms() throws Exception {
        Film film1 = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995, 9,
                        22))
                .duration(-80)
                .id(1)
                .build();
        Film film2 = Film.builder().description("niceFilm").releaseDate(LocalDate.of(2010, 7, 22))
                .duration(90)
                .id(2)
                .build();
        Film film3 = Film.builder().name("Enter the Void").description("niceFilm").releaseDate(LocalDate.of(1019,
                        7, 7))
                .duration(100)
                .id(3)
                .build();
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film3))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("[]".getBytes()));
    }

    @Test
    public void putFilms_whenFilmAttributesAreNotEmptyAndCorrect() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1Update, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().json(objectMapper.writeValueAsString(film1Update)));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenFilmHasIncorrectId() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .id(8)
                .build();
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().bytes("фильма с таким id не суещствует".getBytes()));
    }

    @Test
    public void putFilms_whenFilmHasOnlyName() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("SevenUp")
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1Update, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().json(objectMapper.writeValueAsString(film1Update)));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenNameIsEmpty() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().description("niceFilmUp").releaseDate(LocalDate.of(2000, 9,
                        22))
                .duration(85)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenDescriptionsLengthIs201() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("Seven").description("niceFilmаывацуцтщацущаощкуьмзцдулкузщлппппппп" +
                        "ппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппп" +
                        "ппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппппп").
                releaseDate(LocalDate.of(1995, 9, 22))
                .duration(80)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenReleaseDateIs27December1895() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("Seven").description("niceFilm").
                releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenDurationIsNegative() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("Seven").description("niceFilm").releaseDate(LocalDate.of(1995,
                        9, 22))
                .duration(-1)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilms_whenFilmsAreEmpty() throws Exception {
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .id(1)
                .build();
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().bytes("фильма с таким id не суещствует".getBytes()));
    }

    public void addFilmsForUpdate() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON));
    }
}

