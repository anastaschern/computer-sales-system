
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при отсутствии запрашиваемой записи.
 */
public class NotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
