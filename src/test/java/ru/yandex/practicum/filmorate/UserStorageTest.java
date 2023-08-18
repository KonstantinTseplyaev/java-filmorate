package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserStorageImpl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.yandex.practicum.filmorate.model.enums.FriendStatus.CONFIRMED;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/deleteBd.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserStorageTest {
    private final UserStorageImpl userStorage;
    private final FriendshipStorage friendshipStorage;
    private final UserService userService;
    private User firstUser;
    private User secondUser;
    private User thirdUser;

    @BeforeEach
    public void createUsersModels() {
        firstUser = User.builder().email("myfirstemail@gmail.ru").login("myLog")
                .name("Konstantin").birthday(LocalDate.of(2000, 5, 10))
                .build();
        firstUser.setFriendsStatuses(new HashMap<>());
        secondUser = User.builder().email("SergeyFix.2015@mail.ru").login("Segg")
                .name("Sergey").birthday(LocalDate.of(1998, 11, 23))
                .build();
        secondUser.setFriendsStatuses(new HashMap<>());
        thirdUser = User.builder().email("myThirdmail@gmail.ru").login("ThirdVlad")
                .name("Vladislav").birthday(LocalDate.of(2000, 9, 03))
                .build();
        thirdUser.setFriendsStatuses(new HashMap<>());
    }

    @Test
    public void findUserById_withCorrectIdTest() {
        userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        User actualUser = userStorage.findUserById(user2.getId());
        assertThat(actualUser).hasFieldOrPropertyWithValue("login", "Segg");
    }

    @Test
    public void findUserById_withIncorrectIdTest() {
        assertThatThrownBy(() -> userStorage.findUserById(100)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("пользователя с id " + 100 + " не существует!");
    }

    @Test
    public void createUser_withAllPropertiesTest() {
        User newUser = userService.createUser(firstUser);
        User actualUser = userService.getUserById(newUser.getId());
        assertThat(actualUser).hasFieldOrPropertyWithValue("email", "myfirstemail@gmail.ru")
                .hasFieldOrPropertyWithValue("login", "myLog")
                .hasFieldOrPropertyWithValue("name", "Konstantin")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 5, 10))
                .hasFieldOrPropertyWithValue("friendsStatuses", new HashMap<>());
    }

    @Test
    public void updateUser_Test() {
        User newUser = userService.createUser(firstUser);
        User updateUser = User.builder().email("myNEWemail@gmail.ru").login("myLogNEW")
                .name("KonstantinNEW").birthday(LocalDate.of(2001, 11, 12))
                .build();
        updateUser.setId(newUser.getId());
        User actualUser = userService.updateUser(updateUser);
        assertThat(actualUser).hasFieldOrPropertyWithValue("email", "myNEWemail@gmail.ru")
                .hasFieldOrPropertyWithValue("login", "myLogNEW")
                .hasFieldOrPropertyWithValue("name", "KonstantinNEW")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2001, 11, 12))
                .hasFieldOrPropertyWithValue("friendsStatuses", new HashMap<>());
    }

    @Test
    public void findAllUsers_whenUsersListIsNotEmptyTest() {
        userStorage.createUser(firstUser);
        userStorage.createUser(secondUser);
        userStorage.createUser(thirdUser);
        List<User> users = userStorage.findAllUsers();
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("login", "myLog");
        assertThat(users.get(1)).hasFieldOrPropertyWithValue("login", "Segg");
        assertThat(users.get(2)).hasFieldOrPropertyWithValue("login", "ThirdVlad");
        Assertions.assertEquals(3, users.size());
    }

    @Test
    public void findAllUsers_whenUsersListIsEmptyTest() {
        List<User> users = userStorage.findAllUsers();
        Assertions.assertTrue(users.isEmpty());
    }

    @Test
    public void deleteUserById_withCorrectIdTest() {
        userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        List<User> users = userStorage.findAllUsers();
        Assertions.assertEquals(2, users.size());
        userStorage.deleteUserById(user2.getId());
        List<User> usersActual = userStorage.findAllUsers();
        Assertions.assertEquals(1, usersActual.size());
        assertThat(usersActual.get(0)).hasFieldOrPropertyWithValue("login", "myLog");
    }

    @Test
    public void deleteUserById_withIncorrectIdTest() {
        userStorage.createUser(firstUser);
        userStorage.createUser(secondUser);
        assertThatThrownBy(() -> userStorage.deleteUserById(3)).isInstanceOf(IncorrectIdException.class)
                .hasMessageContaining("пользователя с id " + 3 + " не существует!");
    }

    @Test
    public void getFriendsList_whenUserHasNotFriendsTest() {
        User user1 = userStorage.createUser(firstUser);
        List<User> friends = userStorage.getFriendsList(user1.getId());
        Assertions.assertTrue(friends.isEmpty());
    }

    @Test
    public void getFriendsList_whenUserHasFriendsTest() {
        User user1 = userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        User user3 = userStorage.createUser(thirdUser);
        friendshipStorage.addFriend(user3.getId(), user1.getId(), CONFIRMED);
        friendshipStorage.addFriend(user3.getId(), user2.getId(), CONFIRMED);
        List<User> friends = userStorage.getFriendsList(user3.getId());
        assertThat(friends.get(0)).hasFieldOrPropertyWithValue("login", "myLog");
        assertThat(friends.get(1)).hasFieldOrPropertyWithValue("login", "Segg");
    }

    @Test
    public void getFriendsList_whenFriendHasUpdateNameTest() {
        User user1 = userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        User user3 = userStorage.createUser(thirdUser);
        User updateUser = User.builder().email("myNEWemail@gmail.ru").login("myLogNEW")
                .name("KonstantinNEW").birthday(LocalDate.of(2001, 11, 12))
                .build();
        updateUser.setId(user1.getId());
        friendshipStorage.addFriend(user3.getId(), user1.getId(), CONFIRMED);
        friendshipStorage.addFriend(user3.getId(), user2.getId(), CONFIRMED);
        userStorage.updateUser(updateUser);
        List<User> friends = userStorage.getFriendsList(user3.getId());
        assertThat(friends.get(0)).hasFieldOrPropertyWithValue("login", "myLogNEW");
        assertThat(friends.get(1)).hasFieldOrPropertyWithValue("login", "Segg");
    }

    @Test
    public void getCommonFriends_whenCommonFriendsListIsEmptyTest() {
        User user1 = userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        User user3 = userStorage.createUser(thirdUser);
        friendshipStorage.addFriend(user3.getId(), user1.getId(), CONFIRMED);
        friendshipStorage.addFriend(user1.getId(), user2.getId(), CONFIRMED);
        List<User> commonFriends = userStorage.getCommonFriends(user3.getId(), user1.getId());
        Assertions.assertTrue(commonFriends.isEmpty());
    }

    @Test
    public void getCommonFriends_Test() {
        User user1 = userStorage.createUser(firstUser);
        User user2 = userStorage.createUser(secondUser);
        User user3 = userStorage.createUser(thirdUser);
        friendshipStorage.addFriend(user3.getId(), user1.getId(), CONFIRMED);
        friendshipStorage.addFriend(user2.getId(), user1.getId(), CONFIRMED);
        List<User> commonFriends = userStorage.getCommonFriends(user3.getId(), user2.getId());
        assertThat(commonFriends.get(0)).hasFieldOrPropertyWithValue("login", "myLog");
    }
}
