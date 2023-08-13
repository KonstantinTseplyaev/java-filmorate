package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.enums.FriendStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractModel {
    @NotEmpty(message = "email не может быть пустым")
    @Email(message = "не соответствует формату Email")
    private String email;
    @NotBlank(message = "логин не может быть пустым")
    private String login;
    private String name;
    @Past(message = "дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Map<Long, FriendStatus> friendsStatuses;
}
