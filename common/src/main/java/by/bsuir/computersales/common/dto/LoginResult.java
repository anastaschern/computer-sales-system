
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.model.User;

import java.io.Serializable;

/**
 * Результат успешной авторизации.
 *
 * Содержит токен пользовательской сессии
 * и данные авторизованного пользователя.
 */
public record LoginResult(
        String sessionToken,
        User user
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public LoginResult {
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Токен сессии не может быть пустым"
            );
        }

        if (user == null) {
            throw new IllegalArgumentException(
                    "Данные пользователя не могут быть null"
            );
        }
    }

    /**
     * Возвращает строковое представление без раскрытия токена.
     */
    @Override
    public String toString() {
        return "LoginResult[sessionToken=***, user=" + user + "]";
    }
}
