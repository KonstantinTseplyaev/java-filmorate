package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorageImp;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.yandex.practicum.filmorate.model.enums.FriendStatus.CONFIRMED;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/deleteBd.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FriendshipDbStorageTest {
    private final UserDbStorageImp userDbStorage;
    private final FriendshipDbStorage friendshipDbStorage;
    private final UserService userService;
    private User firstUser;
    private User secondUser;

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
    }

    @Test
    public void addFriend_Test() {
        User user1 = userDbStorage.createUser(firstUser);
        User user2 = userDbStorage.createUser(secondUser);
        friendshipDbStorage.addFriend(user1.getId(), user2.getId(), CONFIRMED);
        User actualUser1 = userService.getUserById(user1.getId());
        assertThat(actualUser1).hasFieldOrPropertyWithValue("friendsStatuses", Map.of(user2.getId(), CONFIRMED));
        User actualUser2 = userService.getUserById(user2.getId());
        assertThat(actualUser2).hasFieldOrPropertyWithValue("friendsStatuses", new HashMap<>());
    }

    @Test
    public void deleteFriend_Test() {
        User user1 = userDbStorage.createUser(firstUser);
        User user2 = userDbStorage.createUser(secondUser);
        friendshipDbStorage.addFriend(user1.getId(), user2.getId(), CONFIRMED);
        User actualUser1 = userService.getUserById(user1.getId());
        assertThat(actualUser1).hasFieldOrPropertyWithValue("friendsStatuses", Map.of(user2.getId(), CONFIRMED));
        friendshipDbStorage.deleteFriend(user1.getId(), user2.getId());
        User ourUser = userService.getUserById(user1.getId());
        assertThat(ourUser).hasFieldOrPropertyWithValue("friendsStatuses", new HashMap<>());
    }
}
