
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.User;

import java.util.Objects;

/**
 * Содержит данные пользователя, необходимые для проверки авторизации.
 *
 * Используется только внутри серверной части приложения.
 * Хеш пароля не передаётся клиенту.
 */
public record UserCredentials(
        User user,
        String passwordHash
) {

    public UserCredentials {
        Objects.requireNonNull(
                user,
                "Пользователь не должен быть null"
        );

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Хеш пароля не должен быть пустым"
            );
        }
    }
}
