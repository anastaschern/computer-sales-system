
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при ошибках аутентификации пользователя.
 */
public class AuthenticationException extends AppException {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение для отсутствующей или недействительной авторизации.
     */
    public AuthenticationException(String message) {
        this(ErrorCode.AUTHENTICATION_REQUIRED, message);
    }

    /**
     * Создаёт исключение с конкретным кодом ошибки аутентификации.
     */
    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);

        if (errorCode != ErrorCode.AUTHENTICATION_REQUIRED
                && errorCode != ErrorCode.INVALID_CREDENTIALS) {
            throw new IllegalArgumentException(
                    "Недопустимый код ошибки аутентификации: " + errorCode
            );
        }
    }
}
