package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserServiceInt {
    private final Storage<User> userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User createUser(User user) {
        checkUsersNameAndLogin(user);
        user.setFriends(new HashSet<>());
        return userStorage.create(user);
    }

    @Override
    public User updateUser(User user) {
        checkUsersNameAndLogin(user);
        Set<Long> friends = userStorage.getById(user.getId()).getFriends();
        user.setFriends(friends);
        return userStorage.update(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    @Override
    public User getUserById(long id) {
        return userStorage.getById(id);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.deleteById(id);
    }

    @Override
    public void deleteAllUsers() {
        userStorage.deleteAll();
    }

    @Override
    public void addFriend(long id, long friendId) {
        checkFriendsId(id, friendId);
        User friend1 = userStorage.getById(id);
        User friend2 = userStorage.getById(friendId);
        friend1.getFriends().add(friendId);
        friend2.getFriends().add(id);
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        checkFriendsId(id, friendId);
        User friend1 = userStorage.getById(id);
        User friend2 = userStorage.getById(friendId);
        if (!friend1.getFriends().contains(friendId)) {
            throw new IncorrectIdException("у вас нет друга под таким id");
        }
        friend1.getFriends().remove(friendId);
        friend2.getFriends().remove(id);
    }

    @Override
    public List<User> getFriendsList(long id) {
        User user = userStorage.getById(id);
        if (user.getFriends().isEmpty()) {
            throw new IncorrectIdException("у этого пользователя нет друзей");
        }
        return user.getFriends().stream()
                .map(userStorage::getById).collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        User user1 = userStorage.getById(id);
        User user2 = userStorage.getById(otherId);
        Set<Long> otherUsersFriends = user2.getFriends();
        return user1.getFriends().stream()
                .filter(otherUsersFriends::contains)
                .map(userStorage::getById).collect(Collectors.toList());
    }

    private void checkUsersNameAndLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("логин не должен содержать пробелы: " + user.getLogin());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkFriendsId(long id, long friendId) {
        if (id == friendId) {
            throw new IncorrectIdException("нужно указать id другого пользователя");
        }
    }
}
