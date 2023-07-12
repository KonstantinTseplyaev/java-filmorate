package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Past;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class Film extends AbstractModel {
    @NotEmpty(message = "название не может быть пустым")
    private String name;
    @Size(max = 200, message = "длина описания не более 200 символов")
    private String description;
    @Past(message = "дата релиза не может быть в будущем")
    private LocalDate releaseDate;
    @PositiveOrZero(message = "длительность должна быть положительной")
    private int duration;
    private Set<Long> likes;
}
