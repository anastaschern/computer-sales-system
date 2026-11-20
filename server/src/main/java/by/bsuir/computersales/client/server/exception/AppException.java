
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Базовое исключение приложения.
 *
 * Содержит код ошибки, который сервер передаёт клиенту,
 * и позволяет сохранить исходную причину возникновения ошибки.
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    /**
     * Создаёт исключение без указания исходной причины.
     */
    public AppException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * Создаёт исключение с указанием исходной причины.
     *
     * @param errorCode код ошибки приложения
     * @param message сообщение об ошибке
     * @param cause исходная причина возникновения ошибки
     */
    public AppException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(message, cause);

        if (errorCode == null) {
            throw new IllegalArgumentException(
                    "Код ошибки не может быть null"
            );
        }

        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
