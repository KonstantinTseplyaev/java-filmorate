package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int currentId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            if (user.getName() == null) {
                user.setName(user.getLogin());
            }
            if (user.getLogin().contains(" ")) {
                log.warn("логин не должен содержать пробелы: {}", user.getLogin());
                throw new ValidationException("логин не должен содержать пробелы");
            }
            user.setId(currentId++);
            users.put(user.getId(), user);
            log.info("добавлен пользователь: {}", user);
            return ResponseEntity.status(201)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user);
        } catch (ValidationException exp) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        try {
            if (user.getLogin().contains(" ")) {
                log.warn("логин не должен содержать пробелы: {}", user.getLogin());
                throw new ValidationException("логин не должен содержать пробелы");
            }
            if (users.containsKey(user.getId())) {
                users.put(user.getId(), user);
                log.info("обновлен пользователь под id {}: {}", user.getId(), user);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(user);
            } else {
                log.warn("пользователя с таким id не существует: {}", user.getId());
                throw new ValidationException("пользователя с таким id не существует");
            }
        } catch (ValidationException exp) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user);
        }
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAddUsers() {
        log.info("кол-во пользователей: {}", users.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users.values());
    }

    @DeleteMapping
    public void deleteAllUsersForTests() {
        users.clear();
        currentId = 1;
    }
}
