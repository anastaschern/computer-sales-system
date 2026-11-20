
package by.bsuir.computersales.client.common.dto;

import by.bsuir.computersales.client.common.model.RoleType;
import by.bsuir.computersales.client.common.util.ValidationUtils;

import java.io.Serializable;

/**
 * Данные для создания пользователя.
 *
 * Содержит логин, пароль и роль нового пользователя.
 */
public record CreateUserData(
        String login,
        String password,
        RoleType role
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public CreateUserData {
        ValidationUtils.validateLogin(login);
        ValidationUtils.validatePassword(password);

        if (role == null) {
            throw new IllegalArgumentException(
                    "Роль пользователя не может быть null"
            );
        }
    }

    /**
     * Возвращает строковое представление без раскрытия пароля.
     */
    @Override
    public String toString() {
        return "CreateUserData[login=" + login
                + ", password=***"
                + ", role=" + role + "]";
    }
}
