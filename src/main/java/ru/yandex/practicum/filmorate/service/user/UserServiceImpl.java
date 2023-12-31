package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.RejectedFriendRequestException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserStorageImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserServiceImpl(UserStorageImpl userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
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
        updatedUser.setFriendsStatuses(friendshipStorage.getFriendsStatuses(user.getId()));
        return updatedUser;
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
        userStorage.findUserById(id);
        userStorage.findUserById(friendId);
        Map<Long, FriendStatus> frStMap = friendshipStorage.getFriendsStatuses(id);
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
        friendshipStorage.addFriend(id, friendId, frStMap.get(id));
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        userStorage.findUserById(id);
        userStorage.findUserById(friendId);
        checkFriendsId(id, friendId);
        if (!friendshipStorage.getFriendsStatuses(id).containsKey(friendId) ||
                friendshipStorage.getFriendsStatuses(id).get(friendId) != FriendStatus.CONFIRMED) {
            throw new IncorrectIdException("у вас нет друга под таким id");
        } else {
            friendshipStorage.deleteFriend(id, friendId);
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
