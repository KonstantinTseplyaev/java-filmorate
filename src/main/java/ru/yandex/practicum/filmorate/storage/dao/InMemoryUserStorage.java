package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage extends Storage<User> {

}

