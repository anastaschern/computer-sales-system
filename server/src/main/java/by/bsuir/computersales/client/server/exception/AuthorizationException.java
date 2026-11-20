
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при отсутствии у пользователя
 * необходимых прав доступа.
 */
public class AuthorizationException extends AppException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}
