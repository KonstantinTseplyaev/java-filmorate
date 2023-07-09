package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Past;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
    private long id;
    @NotEmpty
    private String name;
    @Size(max = 200)
    private String description;
    @Past
    private LocalDate releaseDate;
    @PositiveOrZero
    private int duration;
    private Set<Long> likes;
}
