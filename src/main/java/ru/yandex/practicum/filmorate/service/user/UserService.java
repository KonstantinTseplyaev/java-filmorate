package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserServiceInt {
    private final UserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User createUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("логин не должен содержать пробелы: " + user.getLogin());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setFriends(new HashSet<>());
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("логин не должен содержать пробелы: " + user.getLogin());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userStorage.findUsers();
    }

    @Override
    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.deleteUserById(id);
    }

    @Override
    public void deleteAllUsers() {
        userStorage.deleteAllUsers();
    }

    @Override
    public void addFriend(long id, long friendId) {
        if (id == friendId) {
            throw new IncorrectIdException("нужно указать id другого пользователя");
        }
        User friend1 = userStorage.getUserById(id);
        User friend2 = userStorage.getUserById(friendId);
        friend1.getFriends().add(friendId);
        friend2.getFriends().add(id);
    }

    @Override
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

    @Override
    public List<User> getFriendsList(long id) {
        User user = userStorage.getUserById(id);
        if (user.getFriends().isEmpty()) {
            throw new IncorrectIdException("у этого пользователя нет друзей");
        }
        return user.getFriends().stream()
                .map(userStorage::getUserById).collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        User user1 = userStorage.getUserById(id);
        User user2 = userStorage.getUserById(otherId);
        Set<Long> otherUsersFriends = user2.getFriends();
        return user1.getFriends().stream()
                .filter(otherUsersFriends::contains)
                .map(userStorage::getUserById).collect(Collectors.toList());
    }
}
