package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.RejectedFriendRequestException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserServiceInt {
    private final UserDbStorage userStorage;

    @Autowired
    public UserService(UserDbStorageImpl userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User createUser(User user) {
        checkUsersNameAndLogin(user);
        user.setFriendsStatuses(new HashMap<>());
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        checkUsersNameAndLogin(user);
        Map<Long, FriendStatus> friendStatus = userStorage.findUserById(user.getId()).getFriendsStatuses();
        user.setFriendsStatuses(friendStatus);
        return userStorage.updateUser(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userStorage.findAllUsers();
    }

    @Override
    public User getUserById(long id) {
        return userStorage.findUserById(id);
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
        checkFriendsId(id, friendId);
        User friend1 = userStorage.findUserById(id);
        userStorage.findUserById(friendId);
        Map<Long, FriendStatus> frStMap = friend1.getFriendsStatuses();
        if (frStMap.containsKey(friendId)) {
            checkFriendStatus(frStMap.get(friendId), id);
        }
        /*frStMap.put(id, FriendStatus.UNCONFIRMED);
        int status = new Random().nextInt(2); //псевдорешение пользователя добавлять/недобавлять в друзья
        if (status == 0) {
            frStMap.put(id, FriendStatus.REJECTED);
            throw new RejectedFriendRequestException("Пользователь " + friendId + " отклонил запрос на добавление в друзья пользователя " + id);
        } else {
            frStMap.put(id, FriendStatus.CONFIRMED);
        }*/
        frStMap.put(id, FriendStatus.CONFIRMED);
        userStorage.addFriend(id, friendId, frStMap.get(id));
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        checkFriendsId(id, friendId);
        User fr1 = userStorage.findUserById(id);
        if (!fr1.getFriendsStatuses().containsKey(friendId)) {
            throw new IncorrectIdException("у вас нет друга под таким id");
        }
        if (fr1.getFriendsStatuses().get(friendId) == FriendStatus.CONFIRMED) {
            userStorage.deleteFriend(id, friendId);
        } else {
            throw new IncorrectIdException("у вас нет друга под таким id");
        }
    }

    @Override
    public List<User> getFriendsList(long id) {
        return userStorage.getFriendsList(id);
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        checkFriendsId(id, otherId);
        return userStorage.getCommonFriends(id, otherId);
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

    private void checkFriendStatus(FriendStatus friendStatus, long id) {
        switch (friendStatus.toString()) {
            case "UNCONFIRMED":
                throw new RejectedFriendRequestException("Пользователь " + id + " пока не рассмотрел вашу заявку " +
                        "в друзья");
            case "CONFIRMED":
                throw new RejectedFriendRequestException("Вы уже в друзьях у пользователя " + id);
            case "REJECTED":
                throw new RejectedFriendRequestException("Пользователь " + id + " ранее уже отклонил вашу заявку " +
                        "в друзья");
        }
    }
}
