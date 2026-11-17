
package by.bsuir.computersales.common.dto;

import java.io.Serializable;

/**
 * Данные для авторизации пользователя.
 */
public record LoginRequestData(
        String login,
        String password
) implements Serializable {

    private static final long serialVersionUID = 1L;
}

