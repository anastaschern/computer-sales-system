
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при проверке входных данных.
 */
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION, message);
    }
}
