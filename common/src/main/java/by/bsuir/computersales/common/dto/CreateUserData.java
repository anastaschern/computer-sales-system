
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.model.RoleType;
import java.io.Serializable;

/**
 * Данные для создания пользователя.
 */
public record CreateUserData(
        String login,
        String password,
        RoleType role
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
