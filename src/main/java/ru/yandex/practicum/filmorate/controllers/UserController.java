package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.service.user.UserServiceInt;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/users")
public class UserController {
    private final UserServiceInt userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("запрос на добавление пользователя: {}", user);
        User newUser = userService.createUser(user);
        log.info("добавлен пользователь: {}", newUser);
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(newUser);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        log.info("запрос на обновление пользователя: {}", user);
        User updateUser = userService.updateUser(user);
        log.info("обновлен пользователь под id {}: {}", user.getId(), updateUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateUser);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAllUsers() {
        log.info("запрос на получение всех пользователей");
        Collection<User> users = userService.getAllUsers();
        log.info("пользователи получены. Кол-во пользователей: {}", users.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        log.info("запрос на получение пользователя по id: {}", id);
        User user = userService.getUserById(id);
        log.info("получен пользователь под id: {}", id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable long id) {
        log.info("запрос на удаление пользователя по id: {}", id);
        userService.deleteUserById(id);
        log.info("пользователь под id " + id + " удален");
        return ResponseEntity.ok().body("пользователь под id " + id + " удален");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteAll() {
        log.info("запрос на удаление всех пользователей");
        userService.deleteAllUsers();
        log.info("все пользователи были удалены");
        return ResponseEntity.ok().body("все пользователи были удалены");
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("запрос от пользователя с id {} добавить в друзья пользователя с id {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("пользователь под id {} добавил в друзья пользователя под id {}", id, friendId);
        return ResponseEntity.ok().body("пользователь под id " + friendId + " теперь у вас в друзьях!");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("запрос от пользователя с id {} удалить из друзей пользователя с id {}", id, friendId);
        userService.deleteFriend(id, friendId);
        log.info("пользователь под id {} удалил из друзей пользователя под id {}", id, friendId);
        return ResponseEntity.ok().body("пользователь под id " + friendId + " удален из друзей");
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriendsList(@PathVariable long id) {
        log.info("запрос на получение всех друзей пользователя с id {}", id);
        List<User> friends = userService.getFriendsList(id);
        log.info("все друзья пользователя под id {}: {}", id, friends);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("запрос от пользователя с id {} показать общих друзей с пользователем с id {}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("общие друзья у пользователя {} с пользователем {}: {}", id, otherId, commonFriends);
        return ResponseEntity.ok(commonFriends);
    }
}
