package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

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
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private User user1 = User.builder().email("myfirstemail@gmail.ru").login("myLog")
            .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
            .id(1).build();
    private User user2 = User.builder().email("SergeyFix.2015@mail.ru").login("Segg")
            .name("Sergey").birthday(LocalDate.of(1998, 11, 23))
            .id(2).build();

    @AfterEach
    public void removeUsers() throws Exception {
        mockMvc.perform(delete("/users"));
    }

    @Test
    public void createUserWithCorrectDataTest() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    public void createUserWithOnlyEmailAndLoginTest() throws Exception {
        User newUser = User.builder().email("myEm.2020@mail.ru").login("realGangsta").id(1).build();
        User newUser2 = User.builder().email("myEm.2020@mail.ru").login("realGangsta").id(1).name("realGangsta").build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().is(201))
                .andExpect(content().json(objectMapper.writeValueAsString(newUser2)));
    }

    @Test
    public void createUserWithEmailIsEmptyTest() throws Exception {
        User user1 = User.builder().login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400));
    }

    @Test
    public void createUserWithLoginIsEmptyTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400));
    }

    @Test
    public void createUserWithIncorrectEmail() throws Exception {
        User user1 = User.builder().email("myfirstemailgmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400));
    }

    @Test
    public void createUserWithIncorrectLoginTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("my Log")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400))
                .andExpect(content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    public void createUserWithIncorrectBirthdayTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().plusDays(1))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400));
    }

    @Test
    public void createUserWithBirthdayIsNowMinusOneDayTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().minusDays(1))
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(201));
    }

    @Test
    public void createUserWithUsersBirthdayIsNowTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now())
                .id(1).build();
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().is(400));
    }

    @Test
    public void getUsers_whenUsersAreEmptyTest() throws Exception {
        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("[]".getBytes()));
    }

    @Test
    public void getUsers_whenUsersAreValidTest() throws Exception {
        User user3 = User.builder().email("my3rdmail@gmail.ru").login("myLogin3")
                .name("Maksim").birthday(LocalDate.of(1995, 12, 8))
                .id(3).build();
        Collection<User> usersList = List.of(user1, user2, user3);
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user3))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(usersList)));
    }

    @Test
    public void getUsers_whenUsersAreNotValidTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().plusDays(1))
                .id(1).build();
        User user2 = User.builder().email("myEm.2020@mail.ru").login("my Log")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(2).build();
        User user3 = User.builder().login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(3).build();
        User user4 = User.builder().email("myEm.2020@mail.ru")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(4).build();
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user3))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user4))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("[]".getBytes()));
    }

    @Test
    public void putUserWithCorrectDataTest() throws Exception {
        addUsersForUpdate();
        User userUpdate = User.builder().email("my3rdmail@gmail.ru").login("myLoginUP")
                .name("Maksim").birthday(LocalDate.of(1995, 12, 8))
                .id(1).build();
        Collection<User> filmsList = List.of(userUpdate, user2);
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(userUpdate))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().json(objectMapper.writeValueAsString(userUpdate)));
        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putUserWithNotValidDataTest() throws Exception {
        addUsersForUpdate();
        User user1Up = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().plusDays(1))
                .id(1).build();
        User user2Up = User.builder().email("myEm.2020@mail.ru").login("my Log")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        User user3Up = User.builder().login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        User user4Up = User.builder().email("myEm.2020@mail.ru")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(1).build();
        Collection<User> filmsList = List.of(user1, user2);
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user1Up))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user2Up))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user3Up))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user4Up))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
        mockMvc.perform(
                        get("/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filmsList)));
    }

    @Test
    public void putUsers_whenUserHasIncorrectIdTest() throws Exception {
        addUsersForUpdate();
        User userUp = User.builder().email("myfirstemail@gmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .id(8).build();
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(userUp))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500))
                .andExpect(content().json(objectMapper.writeValueAsString(userUp)));
    }

    private void addUsersForUpdate() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON));
    }
}