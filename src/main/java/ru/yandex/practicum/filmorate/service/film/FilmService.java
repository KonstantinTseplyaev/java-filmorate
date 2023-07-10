package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService implements FilmServiceInt {
    private static final LocalDate LOWER_DATE_LIMIT = LocalDate.of(1895, 12, 28);
    private static final Comparator<Film> TOP_FILMS_COMPARATOR = (f0, f1) -> -1 * Integer.compare(f0.getLikes().size(),
            f1.getLikes().size());
    private final Storage<Film> filmStorage;
    private final Storage<User> userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film createFilm(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LOWER_DATE_LIMIT)) {
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
        film.setLikes(new HashSet<>());
        return filmStorage.create(film);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LOWER_DATE_LIMIT)) {
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
        Set<Long> likes = filmStorage.getById(film.getId()).getLikes();
        film.setLikes(likes);
        return filmStorage.update(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    @Override
    public void deleteFilmById(long id) {
        filmStorage.deleteById(id);
    }

    @Override
    public void deleteAllFilms() {
        filmStorage.deleteAll();
    }

    @Override
    public Film getFilmById(long id) {
        return filmStorage.getById(id);
    }

    @Override
    public int addLikeToFilm(long filmId, long userId) {
        userStorage.getById(userId);
        Film thisFilm = filmStorage.getById(filmId);
        thisFilm.getLikes().add(userId);
        return thisFilm.getLikes().size();
    }

    @Override
    public int deleteLike(long filmId, long userId) {
        userStorage.getById(userId);
        Film thisFilm = filmStorage.getById(filmId);
        if (!thisFilm.getLikes().contains(userId)) {
            throw new IncorrectIdException("пользователь не ставил лайк этому фильму");
        }
        thisFilm.getLikes().remove(userId);
        return thisFilm.getLikes().size();
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(TOP_FILMS_COMPARATOR)
                .limit(count).collect(Collectors.toList());
    }
}
