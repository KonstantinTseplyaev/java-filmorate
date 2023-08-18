package ru.yandex.practicum.filmorate.model.enums;

public enum Mpa {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17");

    final String rating;

    Mpa(String rating) {
        this.rating = rating;
    }
}
