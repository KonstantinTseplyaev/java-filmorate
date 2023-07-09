package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            log.error("логин не должен содержать пробелы: {}", user.getLogin());
            throw new ValidationException("логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User newUser = userService.createUser(user);
        log.info("добавлен пользователь: {}", newUser);
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(newUser);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            log.error("логин не должен содержать пробелы: {}", user.getLogin());
            throw new ValidationException("логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updateUser = userService.updateUser(user);
        log.info("обновлен пользователь под id {}: {}", user.getId(), updateUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateUser);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAllUsers() {
        Collection<User> users = userService.getAllUsers();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(Integer.parseInt(id));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable(required = false) String id) {
        if (id != null) {
            userService.deleteUserById(Integer.parseInt(id));
            return ResponseEntity.ok().body("пользователь под id " + id + " удален");
        } else {
            userService.deleteAllUsers();
            return ResponseEntity.ok().body("все пользователи были удалены");
        }
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> addFriend(@PathVariable String id, @PathVariable String friendId) {
        userService.addFriend(Integer.parseInt(id), Integer.parseInt(friendId));
        log.info("пользователь под id {} добавил в друзья пользователя под id {}", id, friendId);
        return ResponseEntity.ok().body("пользователь под id " + friendId + " теперь у вас в друзьях!");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> deleteFriend(@PathVariable String id, @PathVariable String friendId) {
        userService.deleteFriend(Integer.parseInt(id), Integer.parseInt(friendId));
        log.info("пользователь под id {} удалил из друзей пользователя под id {}", id, friendId);
        return ResponseEntity.ok().body("пользователь под id " + friendId + " удален из друзей");
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriendsList(@PathVariable String id) {
        List<User> friends = userService.getFriendsList(Integer.parseInt(id));
        log.info("все друзья пользователя под id {}: {}", id, friends);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable String id, @PathVariable String otherId) {
        List<User> commonFriends = userService.getCommonFriends(Integer.parseInt(id), Integer.parseInt(otherId));
        log.info("общие друзья у пользователя {} с пользователем {}: {}", id, otherId, commonFriends);
        return ResponseEntity.ok(commonFriends);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidationExpCount(final ValidationException exp) {
        return ResponseEntity.status(400).body((Map.of("error", "Ошибка при валидации", "errorMessage",
                exp.getMessage())));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleIncorrectIDExpCount(final IncorrectIdException exp) {
        return ResponseEntity.status(404).body((Map.of("error", "Ошибка при указании id пользователя",
                "errorMessage",
                exp.getMessage())));
    }
}
