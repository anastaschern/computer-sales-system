
package by.bsuir.computersales.common.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 * Ответ сервера клиенту.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    private final ResponseStatus status;
    private final ErrorCode errorCode;
    private final String message;
    private final Serializable payload;

    public Response(
            UUID requestId,
            ResponseStatus status,
            ErrorCode errorCode,
            String message,
            Serializable payload
    ) {
        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Идентификатор запроса не может быть null"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Статус ответа не может быть null"
            );
        }

        if (status == ResponseStatus.SUCCESS && errorCode != null) {
            throw new IllegalArgumentException(
                    "Успешный ответ не должен содержать код ошибки"
            );
        }

        if (status != ResponseStatus.SUCCESS && errorCode == null) {
            throw new IllegalArgumentException(
                    "Ответ с ошибкой должен содержать код ошибки"
            );
        }

        this.requestId = requestId;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.payload = payload;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getPayload() {
        return payload;
    }

    public static Response success(
            UUID requestId,
            String message,
            Serializable payload
    ) {
        return new Response(
                requestId,
                ResponseStatus.SUCCESS,
                null,
                message,
                payload
        );
    }

    public static Response error(
            UUID requestId,
            ErrorCode errorCode,
            String message
    ) {
        return new Response(
                requestId,
                ResponseStatus.ERROR,
                errorCode,
                message,
                null
        );
    }

    public static Response unauthorized(
            UUID requestId,
            ErrorCode errorCode,
            String message
    ) {
        return new Response(
                requestId,
                ResponseStatus.UNAUTHORIZED,
                errorCode,
                message,
                null
        );
    }
}
