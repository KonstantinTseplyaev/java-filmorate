package ru.yandex.practicum.filmorate.storage.old_storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage extends Storage<Film> {

}