package ru.yandex.practicum.filmorate.storage.user;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    Collection<User> findUsers();

    User getUserById(long id);

    void deleteUserById(long id);

    void deleteAllUsers();
}