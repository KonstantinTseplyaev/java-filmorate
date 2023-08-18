package ru.yandex.practicum.filmorate.exceptions;

public class RejectedFriendRequestException extends RuntimeException {
    public RejectedFriendRequestException(String message) {
        super(message);
    }
}
