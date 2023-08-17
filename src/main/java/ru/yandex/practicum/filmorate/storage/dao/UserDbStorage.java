package ru.yandex.practicum.filmorate.storage.dao;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDbStorage {
    User findUserById(long id);

    User createUser(User user);

    User updateUser(User user);

    List<User> findAllUsers();

    void deleteUserById(long id);

    void deleteAllUsers();

    List<User> getFriendsList(long id);

    List<User> getCommonFriends(long id, long otherId);
}
