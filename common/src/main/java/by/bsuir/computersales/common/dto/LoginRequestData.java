
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.util.ValidationUtils;

import java.io.Serializable;

/**
 * Данные для авторизации пользователя.
 */
public record LoginRequestData(
        String login,
        String password
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public LoginRequestData {
        ValidationUtils.validateLogin(login);
        ValidationUtils.validatePassword(password);
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
