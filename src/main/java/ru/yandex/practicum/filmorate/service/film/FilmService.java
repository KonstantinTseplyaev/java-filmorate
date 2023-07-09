package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectIdException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
        return filmStorage.creteFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LOWER_DATE_LIMIT)) {
            throw new ValidationException("недопустимая дата релиза: " + film.getReleaseDate());
        }
        Set<Long> likes = filmStorage.getFilmById(film.getId()).getLikes();
        film.setLikes(likes);
        return filmStorage.updateFilm(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.findFilms();
    }

    @Override
    public void deleteFilmById(long id) {
        filmStorage.deleteFilmById(id);
    }

    @Override
    public void deleteAllFilms() {
        filmStorage.deleteAllFilms();
    }

    @Override
    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    @Override
    public int addLikeToFilm(long filmId, long userId) {
        userStorage.getUserById(userId);
        Film thisFilm = filmStorage.getFilmById(filmId);
        thisFilm.getLikes().add(userId);
        return thisFilm.getLikes().size();
    }

    @Override
    public int deleteLike(long filmId, long userId) {
        userStorage.getUserById(userId);
        Film thisFilm = filmStorage.getFilmById(filmId);
        if (!thisFilm.getLikes().contains(userId)) {
            throw new IncorrectIdException("пользователь не ставил лайк этому фильму");
        }
        thisFilm.getLikes().remove(userId);
        return thisFilm.getLikes().size();
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return filmStorage.findFilms().stream()
                .sorted(TOP_FILMS_COMPARATOR)
                .limit(count).collect(Collectors.toList());
    }
}
