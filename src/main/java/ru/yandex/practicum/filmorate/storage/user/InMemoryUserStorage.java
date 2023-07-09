package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new IncorrectIdException("Пользователя с таким id не существует: " + user.getId());
        }
        Set<Long> listFriends = users.get(user.getId()).getFriends();
        user.setFriends(listFriends);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> findUsers() {
        return users.values();
    }

    @Override
    public User getUserById(long id) {
        if (!users.containsKey(id)) {
            throw new IncorrectIdException("Пользователя с таким id не существует: " + id);
        }
        return users.get(id);
    }

    @Override
    public void deleteUserById(long id) {
        if (!users.containsKey(id)) {
            throw new IncorrectIdException("Пользователя с таким id не существует: " + id);
        }
        users.remove(id);
    }

    @Override
    public void deleteAllUsers() {
        users.clear();
    }

    @Override
    public List<User> getFriendsList(long id) {
        if (!users.containsKey(id)) {
            throw new IncorrectIdException("Пользователя с таким id не существует: " + id);
        }
        User user = users.get(id);
        if (user.getFriends().isEmpty()) {
            throw new IncorrectIdException("у этого пользователя нет друзей");
        }
        return user.getFriends().stream()
                .map(users::get).collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        if (!users.containsKey(id) || !users.containsKey(otherId)) {
            throw new IncorrectIdException("Пользователя с таким id не существует");
        }
        Set<Long> otherUsersFriends = users.get(otherId).getFriends();
        return users.get(id).getFriends().stream()
                .filter(otherUsersFriends::contains)
                .map(users::get).collect(Collectors.toList());
    }
}