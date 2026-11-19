
package by.bsuir.computersales.common.util;

/**
 * Содержит общие методы проверки входных данных.
 */
public final class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private ValidationUtils() {
        // Запрещаем создание экземпляров класса.
    }

    /**
     * Проверяет корректность логина.
     *
     * Логин должен содержать от 3 до 30 символов:
     * латинские буквы, цифры, точку, дефис или подчёркивание.
     */
    public static void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым"
            );
        }

        if (!login.matches("[a-zA-Z0-9._-]{3,30}")) {
            throw new IllegalArgumentException(
                    "Логин должен содержать от 3 до 30 символов: "
                            + "латинские буквы, цифры, точку, дефис "
                            + "или подчёркивание"
            );
        }
    }

    /**
     * Проверяет корректность пароля.
     *
     * Пароль должен содержать не менее 8 символов.
     */
    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль не может быть пустым"
            );
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать не менее 8 символов"
            );
        }
    }
}
