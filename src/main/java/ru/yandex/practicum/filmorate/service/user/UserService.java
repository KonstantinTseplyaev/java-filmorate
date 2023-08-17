package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {

    User createUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getUserById(long id);

    void deleteUserById(long id);

    void deleteAllUsers();

    void addFriend(long id, long friendId);

    void deleteFriend(long id, long friendId);

    List<User> getFriendsList(long id);

    List<User> getCommonFriends(long id, long otherId);
}
