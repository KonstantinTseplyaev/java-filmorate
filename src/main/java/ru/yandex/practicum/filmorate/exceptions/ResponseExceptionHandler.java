package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ResponseExceptionHandler extends DefaultHandlerExceptionResolver {
    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationExpCount(final ValidationException exp) {
        log.error(exp.getMessage());
        return ResponseEntity.status(400).body((Map.of("error", "Ошибка при валидации", "errorMessage",
                exp.getMessage())));
    }

    @ExceptionHandler(value = IncorrectIdException.class)
    public ResponseEntity<Map<String, String>> handleIncorrectIDExpCount(final IncorrectIdException exp) {
        log.error(exp.getMessage());
        return ResponseEntity.status(404)
                .body((Map.of("error", "Ошибка при указании id фильма/пользователя", "errorMessage",
                        exp.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        final List<Violation> violations = exp.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        log.error(violations.get(0).message);
        return new ValidationErrorResponse(violations);
    }

    @Getter
    @RequiredArgsConstructor
    public static class ValidationErrorResponse {
        private final List<Violation> violations;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Violation {
        private final String fieldName;
        private final String message;
    }
}
