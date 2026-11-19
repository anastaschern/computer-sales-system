
package by.bsuir.computersales.server.exception;

import by.bsuir.computersales.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при ошибках доступа к базе данных.
 *
 * Используется для обработки ошибок JDBC и SQL.
 * Сохраняет исходную причину ошибки для серверного журнала.
 */
public class DataAccessException extends AppException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL, message, cause);
    }
}
