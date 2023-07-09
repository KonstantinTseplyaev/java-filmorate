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
    public void createFilmWithCorrectDataTest() throws Exception {
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(film1)));
    }

    @Test
    public void createFilmWithNameOnlyTest() throws Exception {
        Film film = Film.builder().name("Melancholy").id(1).build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void createFilmWithNameIsEmptyTest() throws Exception {
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
    public void createFilmWithDescriptionsLengthIs201Test() throws Exception {
        Film film = Film.builder().name("Seven").description("x".repeat(201))
                .releaseDate(LocalDate.of(1995, 9, 22))
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
    public void createFilmWithDescriptionsLengthIs200Test() throws Exception {
        Film film = Film.builder().name("Seven").description("x".repeat(200))
                .releaseDate(LocalDate.of(1995, 9, 22))
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
    public void createFilmWithReleaseDateIs27December1895Test() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .id(1)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    public void createFilmWithReleaseDateIs28December1895Test() throws Exception {
        Film film = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 28))
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
    public void createFilmWithDurationIsZeroTest() throws Exception {
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
    public void createFilmWithDurationIsNegativeTest() throws Exception {
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
    public void getFilms_whenFilmsAreEmptyTest() throws Exception {
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("[]".getBytes()));
    }

    @Test
    public void getFilms_whenFilmsAreValidTest() throws Exception {
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
    public void getFilms_whenFilmsAreNotValidTest() throws Exception {
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
    public void putFilm_withCorrectDataTest() throws Exception {
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
    public void putFilm_withIncorrectIdTest() throws Exception {
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
                .andExpect(status().is(500))
                .andExpect(content().json(objectMapper.writeValueAsString(film1Update)));
    }

    @Test
    public void putFilm_withNameOnlyTest() throws Exception {
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
    public void putFilm_withNameIsEmptyTest() throws Exception {
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
    public void putFilm_withDescriptionsLengthIs201Test() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("Seven").description("x".repeat(201))
                .releaseDate(LocalDate.of(1995, 9, 22))
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
    public void putFilm_withReleaseDateIs27December1895Test() throws Exception {
        addFilmsForUpdate();
        Film film1Update = Film.builder().name("Seven").description("niceFilm")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(80)
                .id(1)
                .build();
        Collection<Film> filmsList = List.of(film1, film2);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
        mockMvc.perform(
                        get("/films")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putFilm_withDurationIsNegativeTest() throws Exception {
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
    public void putFilm_whenFilmsAreEmptyTest() throws Exception {
        Film film1Update = Film.builder().name("SevenUp").description("niceFilmUp").releaseDate(LocalDate.of(2000,
                        9, 22))
                .duration(85)
                .id(1)
                .build();
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film1Update))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(content().json(objectMapper.writeValueAsString(film1Update)));
    }

    private void addFilmsForUpdate() throws Exception {
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

