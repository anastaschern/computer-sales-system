
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.model.User;
import java.io.Serializable;

/**
 * Результат успешной авторизации.
 */
public record LoginResult(
        String sessionToken,
        User user
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
