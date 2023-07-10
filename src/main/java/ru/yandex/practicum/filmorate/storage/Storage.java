package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.model.AbstractModel;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Storage<T extends AbstractModel> {
    private long currentId = 0;
    private final Map<Long, T> dateMap = new HashMap<>();

    public T create(T t) {
        t.setId(++currentId);
        dateMap.put(t.getId(), t);
        return t;
    }

    public T update(T t) {
        if (!dateMap.containsKey(t.getId()) && t.getClass().equals(Film.class)) {
            throw new IncorrectIdException("Фильма с таким id не существует: " + t.getId());
        }
        if (!dateMap.containsKey(t.getId()) && t.getClass().equals(User.class)) {
            throw new IncorrectIdException("Пользователя с таким id не существует: " + t.getId());
        }
        dateMap.put(t.getId(), t);
        return t;
    }

    public void deleteById(long id) {
        if (!dateMap.containsKey(id)) {
            throw new IncorrectIdException("Такого id нет: " + id);
        }
        dateMap.remove(id);
    }

    public void deleteAll() {
        dateMap.clear();
    }

    public Collection<T> getAll() {
        return dateMap.values();
    }

    public T getById(long id) {
        if (!dateMap.containsKey(id)) {
            throw new IncorrectIdException("Такого id нет: " + id);
        }
        return dateMap.get(id);
    }
}
