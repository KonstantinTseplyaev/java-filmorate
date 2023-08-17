package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.RejectedFriendRequestException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImp implements UserService {
    private final UserDbStorage userStorage;

    private final FriendshipDbStorage friendshipDbStorage;

    @Autowired
    public UserServiceImp(UserDbStorageImp userStorage, FriendshipDbStorage friendshipDbStorage) {
        this.userStorage = userStorage;
        this.friendshipDbStorage = friendshipDbStorage;
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
        User updatedUser = userStorage.updateUser(user);
        updatedUser.setFriendsStatuses(friendshipDbStorage.getFriendsStatuses(user.getId()));
        return updatedUser;
    }

    @Override
    public Collection<User> getAllUsers() {
        List<User> users = userStorage.findAllUsers();
        for (User user : users) {
            user.setFriendsStatuses(friendshipDbStorage.getFriendsStatuses(user.getId()));
        }
        return users;
    }

    @Override
    public User getUserById(long id) {
        User user = userStorage.findUserById(id);
        user.setFriendsStatuses(friendshipDbStorage.getFriendsStatuses(id));
        return user;
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
        userStorage.findUserById(id);
        userStorage.findUserById(friendId);
        Map<Long, FriendStatus> frStMap = friendshipDbStorage.getFriendsStatuses(id);
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
        friendshipDbStorage.addFriend(id, friendId, frStMap.get(id));
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        userStorage.findUserById(id);
        userStorage.findUserById(friendId);
        checkFriendsId(id, friendId);
        if (!friendshipDbStorage.getFriendsStatuses(id).containsKey(friendId) ||
                friendshipDbStorage.getFriendsStatuses(id).get(friendId) != FriendStatus.CONFIRMED) {
            throw new IncorrectIdException("у вас нет друга под таким id");
        } else {
            friendshipDbStorage.deleteFriend(id, friendId);
        }
    }

    @Override
    public List<User> getFriendsList(long id) {
        List<User> users = userStorage.getFriendsList(id);
        for (User user : users) {
            user.setFriendsStatuses(friendshipDbStorage.getFriendsStatuses(user.getId()));
        }
        return users;
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        checkFriendsId(id, otherId);
        List<User> users = userStorage.getCommonFriends(id, otherId);
        for (User user : users) {
            user.setFriendsStatuses(friendshipDbStorage.getFriendsStatuses(user.getId()));
        }
        return users;
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
