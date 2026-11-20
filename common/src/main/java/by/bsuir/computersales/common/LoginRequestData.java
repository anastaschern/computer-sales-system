
package by.bsuir.computersales.common;

import java.io.Serializable;

/**
 * Данные для авторизации пользователя.
 *
 * При входе проверяется только наличие логина и пароля.
 * Проверка правильности учётных данных выполняется сервером.
 */
public record LoginRequestData(
        String login,
        String password
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public LoginRequestData {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым"
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль не может быть пустым"
            );
        }
    }

    /**
     * Возвращает строковое представление без раскрытия пароля.
     */
    @Override
    public String toString() {
        return "LoginRequestData[login=" + login
                + ", password=***]";
    }
}
