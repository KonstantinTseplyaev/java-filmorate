package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;

import java.util.HashMap;
import java.util.Map;

@Component
public class FriendshipDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(long id, long friendId, FriendStatus friendStatus) {
        jdbcTemplate.update("insert into friendship_statuses(user_id, friend_id, status) values (?, ?, ?)", id,
                friendId, friendStatus.toString());
    }

    public void deleteFriend(long id, long friendId) {
        jdbcTemplate.update("delete from friendship_statuses where user_id = ? and friend_id = ?", id, friendId);
        jdbcTemplate.update("delete from friendship_statuses where user_id = ? and friend_id = ?", friendId, id);
    }

    public Map<Long, FriendStatus> getFriendsStatuses(long userId) {
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
