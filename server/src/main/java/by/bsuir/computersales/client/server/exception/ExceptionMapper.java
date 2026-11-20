
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;
import by.bsuir.computersales.client.common.protocol.Request;
import by.bsuir.computersales.client.common.protocol.Response;

/**
 * Преобразует исключения приложения в ответы сервера.
 */
public final class ExceptionMapper {

    private ExceptionMapper() {
        // Запрещаем создание экземпляров класса.
    }

    /**
     * Преобразует исключение в ответ клиенту.
     */
    public static Response toResponse(
            Request request,
            Exception exception
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Запрос не может быть null"
            );
        }

        if (exception == null) {
            throw new IllegalArgumentException(
                    "Исключение не может быть null"
            );
        }

        // Ошибки аутентификации возвращаются со статусом UNAUTHORIZED.
        if (exception instanceof AuthenticationException authenticationException) {
            return Response.unauthorized(
                    request.getRequestId(),
                    authenticationException.getErrorCode(),
                    authenticationException.getMessage()
            );
        }

        // Обрабатываем остальные ошибки приложения.
        if (exception instanceof AppException appException) {

            // Внутренние ошибки не раскрывают подробности клиенту.
            if (appException.getErrorCode() == ErrorCode.INTERNAL) {
                return Response.error(
                        request.getRequestId(),
                        ErrorCode.INTERNAL,
                        "Внутренняя ошибка сервера"
                );
            }

            return Response.error(
                    request.getRequestId(),
                    appException.getErrorCode(),
                    appException.getMessage()
            );
        }

        // Неизвестные исключения также скрывают внутренние подробности.
        return Response.error(
                request.getRequestId(),
                ErrorCode.INTERNAL,
                "Внутренняя ошибка сервера"
        );
    }
}
