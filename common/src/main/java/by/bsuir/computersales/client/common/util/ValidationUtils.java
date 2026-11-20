
package by.bsuir.computersales.client.common.util;

import java.util.regex.Pattern;

/**
 * Содержит общие методы проверки входных данных.
 */
public final class ValidationUtils {

    private static final int MIN_LOGIN_LENGTH = 3;
    private static final int MAX_LOGIN_LENGTH = 30;

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Допустимые символы логина:
     * латинские буквы, цифры, точка, дефис и подчёркивание.
     */
    private static final Pattern LOGIN_PATTERN =
            Pattern.compile("[a-zA-Z0-9._-]+");

    private ValidationUtils() {
        // Запрещаем создание экземпляров класса.
    }

    /**
     * Проверяет корректность логина.
     */
    public static void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым"
            );
        }

        if (login.length() < MIN_LOGIN_LENGTH
                || login.length() > MAX_LOGIN_LENGTH
                || !LOGIN_PATTERN.matcher(login).matches()) {

            throw new IllegalArgumentException(
                    "Логин должен содержать от 3 до 30 символов: "
                            + "латинские буквы, цифры, точку, дефис "
                            + "или подчёркивание"
            );
        }
    }

    /**
     * Проверяет корректность пароля при создании пользователя
     * или изменении пароля.
     */
    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль не может быть пустым"
            );
        }

        if (password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {

            throw new IllegalArgumentException(
                    "Пароль должен содержать от 8 до 128 символов"
            );
        }
    }
}
