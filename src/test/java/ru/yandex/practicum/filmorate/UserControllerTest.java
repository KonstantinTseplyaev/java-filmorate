package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.exceptions.ResponseExceptionHandler;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserServiceInt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private UserController controller;
    @Autowired
    private UserServiceInt service;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private User user1;
    private User user2;

    @BeforeEach
    public void createModels() {
        user1 = User.builder().email("myfirstemail@gmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        user2 = User.builder().email("SergeyFix.2015@mail.ru").login("Segg")
                .name("Sergey").birthday(LocalDate.of(1998, 11, 23))
                .build();
        user1.setId(1);
        user2.setId(2);
    }

    @AfterEach
    public void removeUsers() throws Exception {
        service.deleteAllUsers();
    }

    @Test
    public void createUser_whenCorrectDataTest() throws Exception {
        service.createUser(user1);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users/" + user1.getId()));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    public void createUser_whenOnlyEmailAndLoginTest() throws Exception {
        User newUser = User.builder().email("myEm.2020@mail.ru").login("realGangsta").build();
        service.createUser(newUser);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users/" + newUser.getId()));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(newUser)));
    }

    @Test
    public void createUser_whenEmailIsEmptyTest() throws Exception {
        User user1 = User.builder().login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("email", "email не может быть пустым"))))));
    }

    @Test
    public void createUser_whenLoginIsEmptyTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("login", "логин не может быть пустым"))))));
    }

    @Test
    public void createUser_whenIncorrectEmail() throws Exception {
        User user1 = User.builder().email("myfirstemailgmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("email", "не соответствует формату Email"))))));
    }

    @Test
    public void createUser_whenIncorrectLoginTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("my Log")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error",
                "Ошибка при валидации", "errorMessage", "логин не должен содержать пробелы: " + user1.getLogin()))));
    }

    @Test
    public void createUser_whenIncorrectBirthdayTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().plusDays(1))
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("birthday", "дата рождения не может быть в будущем"))))));
    }

    @Test
    public void createUser_whenBirthdayIsNowMinusOneDayTest() throws Exception {
        User newUser = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().minusDays(1))
                .build();
        service.createUser(newUser);
        newUser.setFriends(new HashSet<>());
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users/" + newUser.getId()));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(newUser)));
    }

    @Test
    public void createUser_whenUsersBirthdayIsNowTest() throws Exception {
        User user1 = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now())
                .build();
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("birthday", "дата рождения не может быть в будущем"))))));
    }

    @Test
    public void getUsers_whenUsersAreEmptyTest() throws Exception {
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users"));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json("[]"));
    }

    @Test
    public void getUsers_whenUsersAreValidTest() throws Exception {
        User user3 = User.builder().email("my3rdmail@gmail.ru").login("myLogin3")
                .name("Maksim").birthday(LocalDate.of(1995, 12, 8))
                .build();
        user3.setId(3);
        Collection<User> usersList = List.of(user1, user2, user3);
        service.createUser(user1);
        service.createUser(user2);
        service.createUser(user3);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users"));
        response.andExpect(MockMvcResultMatchers.status().is(200));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(usersList)));
    }

    @Test
    public void putUserWithCorrectDataTest() throws Exception {
        addUsersForUpdate();
        User userUpdate = User.builder().email("my3rdmail@gmail.ru").login("myLoginUP")
                .name("Maksim").birthday(LocalDate.of(1995, 12, 8))
                .build();
        userUpdate.setId(user1.getId());
        userUpdate.setFriends(new HashSet<>());
        Collection<User> usersList = List.of(userUpdate, user2);
        ResultActions putResponse = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdate)));
        putResponse.andExpect(MockMvcResultMatchers.status().is(200));
        putResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(userUpdate)));
        ResultActions getResponse = mockMvc.perform(MockMvcRequestBuilders.get("/users"));
        getResponse.andExpect(MockMvcResultMatchers.status().is(200));
        getResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(usersList)));
    }

    @Test
    public void putUserWithNotValidDataTest() throws Exception {
        addUsersForUpdate();
        User user1Up = User.builder().email("myEm.2020@mail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.now().plusDays(1))
                .build();
        user1Up.setId(1);
        User user2Up = User.builder().email("myEm.2020@mail.ru").login("my Log")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        user2Up.setId(1);
        User user3Up = User.builder().login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        user3Up.setId(1);
        User user4Up = User.builder().email("myEm.2020@mail.ru")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        user4Up.setId(1);
        Collection<User> usersList = List.of(user1, user2);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Up)));
        response.andExpect(MockMvcResultMatchers.status().is(400));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("birthday", "дата рождения не может быть в будущем"))))));
        ResultActions response2 = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Up)));
        response2.andExpect(MockMvcResultMatchers.status().is(400));
        response2.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error",
                "Ошибка при валидации", "errorMessage", "логин не должен содержать пробелы: " + user2Up.getLogin()))));
        ResultActions response3 = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user3Up)));
        response3.andExpect(MockMvcResultMatchers.status().is(400));
        response3.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("email", "email не может быть пустым"))))));
        ResultActions response4 = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user4Up)));
        response4.andExpect(MockMvcResultMatchers.status().is(400));
        response4.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(
                new ResponseExceptionHandler.ValidationErrorResponse(List.of(
                        new ResponseExceptionHandler.Violation("login", "логин не может быть пустым"))))));
        ResultActions getResponse = mockMvc.perform(MockMvcRequestBuilders.get("/users"));
        getResponse.andExpect(MockMvcResultMatchers.status().is(200));
        getResponse.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(usersList)));
    }

    @Test
    public void putUsers_whenUserHasIncorrectIdTest() throws Exception {
        addUsersForUpdate();
        User userUp = User.builder().email("myfirstemail@gmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        userUp.setId(8);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUp)));
        response.andExpect(MockMvcResultMatchers.status().is(404));
        response.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(Map.of("error",
                "Ошибка при указании id фильма/пользователя", "errorMessage", "Такого id нет: " + userUp.getId()))));
    }

    private void addUsersForUpdate() throws Exception {
        service.createUser(user1);
        service.createUser(user2);
    }
}
