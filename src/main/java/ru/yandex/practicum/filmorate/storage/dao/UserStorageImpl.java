package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserStorageImpl implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendshipStorage friendshipStorage;

    public UserStorageImpl(JdbcTemplate jdbcTemplate, FriendshipStorage friendshipStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.friendshipStorage = friendshipStorage;
    }

    @Override
    public  User findUserById(long id) {
        try {
            String sql = "select * from users where user_id = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUser(rs), id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("пользователя с id " + id + " не существует!");
        }
    }

    @Override
    public User createUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        long id = simpleJdbcInsert.executeAndReturnKey(toMap(user)).longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User updateUser) {
        jdbcTemplate.update("update users set email = ?, login = ?, name = ?, birthday = ? where user_id = ?",
                updateUser.getEmail(), updateUser.getLogin(), updateUser.getName(), updateUser.getBirthday(),
                updateUser.getId());
        return findUserById(updateUser.getId());
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public void deleteUserById(long id) {
        try {
            findUserById(id);
            jdbcTemplate.update("delete from users where user_id = ?", id);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("пользователя с id " + id + " не существует!");
        }
    }

    @Override
    public void deleteAllUsers() {
        jdbcTemplate.update("delete from users");
    }

    @Override
    public List<User> getFriendsList(long id) {
        String sql = "select * from users where user_id in " +
                "(select friend_id from friendship_statuses where user_id = ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        try {
            String sql = "select * from users where user_id in " +
                    "(select friend_id from friendship_statuses where user_id = ? and friend_id in " +
                    "(select friend_id from friendship_statuses where user_id = ?))";
            return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id, otherId);
        } catch (EmptyResultDataAccessException exp) {
            throw new IncorrectIdException("пользователя с таким id не существует!");
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        long id = rs.getInt("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        Map<Long, FriendStatus> friendsStatuses = friendshipStorage.getFriendsStatuses(id);
        LocalDate birthday = null;
        if (rs.getDate("birthday") != null) {
            birthday = rs.getDate("birthday").toLocalDate();
        }
        User user = User.builder().email(email).login(login).name(name).birthday(birthday)
                .friendsStatuses(friendsStatuses).build();
        user.setId(id);
        return user;
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }
}
