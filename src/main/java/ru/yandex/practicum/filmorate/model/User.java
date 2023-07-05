package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private int id;
    @NotEmpty
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
}
