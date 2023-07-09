package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private long currentId = 0;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        user.setId(++currentId);
        user.setFriends(new HashSet<>());
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.findUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public void deleteUserById(long id) {
        userStorage.deleteUserById(id);
    }

    public void deleteAllUsers() {
        userStorage.deleteAllUsers();
    }

    public void addFriend(long id, long friendId) {
        if (id == friendId) {
            throw new IncorrectIdException("нужно указать id другого пользователя");
        }
        User friend1 = userStorage.getUserById(id);
        User friend2 = userStorage.getUserById(friendId);
        friend1.getFriends().add(friendId);
        friend2.getFriends().add(id);
    }

    public void deleteFriend(long id, long friendId) {
        if (id == friendId) {
            throw new IncorrectIdException("нужно указать id другого пользователя");
        }
        User friend1 = userStorage.getUserById(id);
        User friend2 = userStorage.getUserById(friendId);
        if (!friend1.getFriends().contains(friendId)) {
            throw new IncorrectIdException("у вас нет друга под таким id");
        }
        friend1.getFriends().remove(friendId);
        friend2.getFriends().remove(id);
    }

    public List<User> getFriendsList(long id) {
        return userStorage.getFriendsList(id);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }
}
