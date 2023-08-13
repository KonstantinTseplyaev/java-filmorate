package ru.yandex.practicum.filmorate.storage.dao;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;

import java.util.List;

public interface UserDbStorage {
    User findUserById(long id);

    User createUser(User user);

    User updateUser(User user);

    List<User> findAllUsers();

    void deleteUserById(long id);

    void deleteAllUsers();

    void addFriend(long id, long friendId, FriendStatus friendStatus);

    void deleteFriend(long id, long friendId);

    List<User> getFriendsList(long id);

    List<User> getCommonFriends(long id, long otherId);
}
