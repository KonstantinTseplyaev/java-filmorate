package ru.yandex.practicum.filmorate.storage.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDbStorageImpl implements UserDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        createFriendsStatuses(id, user);
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
    public void addFriend(long id, long friendId, FriendStatus friendStatus) {
        jdbcTemplate.update("insert into friendship_statuses(user_id, friend_id, status) values (?, ?, ?)", id,
                friendId, friendStatus.toString());
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        jdbcTemplate.update("delete from friendship_statuses where user_id = ? and friend_id = ?", id, friendId);
        jdbcTemplate.update("delete from friendship_statuses where user_id = ? and friend_id = ?", friendId, id);
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
        LocalDate birthday = null;
        if (rs.getDate("birthday") != null) {
            birthday = rs.getDate("birthday").toLocalDate();
        }
        Map<Long, FriendStatus> friendsStatuses = getFriendsStatuses(id);
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

    private void createFriendsStatuses(long userId, User user) {
        for (long frSt : user.getFriendsStatuses().keySet()) {
            String sqlRequest = "insert into friendship_statuses(user_id, friend_id, status) values (?, ?, ?)";
            jdbcTemplate.update(sqlRequest, userId, frSt, user.getFriendsStatuses().get(frSt));
        }
    }

    private Map<Long, FriendStatus> getFriendsStatuses(long userId) {
        SqlRowSet statusesRows = jdbcTemplate.queryForRowSet("select friend_id, status from friendship_statuses " +
                "where user_id = ?", userId);
        Map<Long, FriendStatus> friendStatusMap = new HashMap<>();
        while (statusesRows.next()) {
            friendStatusMap.put((long) statusesRows.getInt("friend_id"),
                    FriendStatus.valueOf(statusesRows.getString("status")));
        }
        return friendStatusMap;
    }
}
