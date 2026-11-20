
package by.bsuir.computersales.server.exception;

import by.bsuir.computersales.common.protocol.ErrorCode;
import by.bsuir.computersales.common.protocol.Request;
import by.bsuir.computersales.common.protocol.RequestType;
import by.bsuir.computersales.common.protocol.Response;
import by.bsuir.computersales.common.protocol.ResponseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionMapperTest {

    private Request createRequest() {
        return new Request(
                RequestType.GET_PRODUCTS,
                null,
                null
        );
    }

    @Test
    void shouldMapValidationException() {
        Request request = createRequest();

        Response response = ExceptionMapper.toResponse(
                request,
                new ValidationException("Некорректные данные")
        );

        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.VALIDATION, response.getErrorCode());
        assertEquals("Некорректные данные", response.getMessage());
    }

    @Test
    void shouldMapAuthenticationExceptionAsUnauthorized() {
        Request request = createRequest();

        Response response = ExceptionMapper.toResponse(
                request,
                new AuthenticationException("Требуется авторизация")
        );

        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(ResponseStatus.UNAUTHORIZED, response.getStatus());
        assertEquals(
                ErrorCode.AUTHENTICATION_REQUIRED,
                response.getErrorCode()
        );
    }

    @Test
    void shouldMapInvalidCredentialsAsUnauthorized() {
        Request request = createRequest();

        Response response = ExceptionMapper.toResponse(
                request,
                new AuthenticationException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Неверный логин или пароль"
                )
        );

        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(ResponseStatus.UNAUTHORIZED, response.getStatus());
        assertEquals(
                ErrorCode.INVALID_CREDENTIALS,
                response.getErrorCode()
        );
        assertEquals("Неверный логин или пароль", response.getMessage());
    }

    @Test
    void shouldMapAuthorizationException() {
        Response response = ExceptionMapper.toResponse(
                createRequest(),
                new AuthorizationException("Недостаточно прав")
        );

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.ACCESS_DENIED, response.getErrorCode());
    }

    @Test
    void shouldMapNotFoundException() {
        Response response = ExceptionMapper.toResponse(
                createRequest(),
                new NotFoundException("Товар не найден")
        );

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.NOT_FOUND, response.getErrorCode());
    }

    @Test
    void shouldMapBusinessRuleException() {
        Response response = ExceptionMapper.toResponse(
                createRequest(),
                new BusinessRuleException("Операция запрещена")
        );

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.BUSINESS_RULE, response.getErrorCode());
    }

    @Test
    void shouldMapInsufficientStockException() {
        Response response = ExceptionMapper.toResponse(
                createRequest(),
                new InsufficientStockException("Недостаточно товара")
        );

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(
                ErrorCode.INSUFFICIENT_STOCK,
                response.getErrorCode()
        );
    }


    @Test
    void shouldMapDataAccessException() {
        Response response = ExceptionMapper.toResponse(
                createRequest(),
                new DataAccessException(
                        "Ошибка доступа к базе данных",
                        new RuntimeException("Database connection failed")
                )
        );

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.INTERNAL, response.getErrorCode());

        assertEquals(
                "Внутренняя ошибка сервера",
                response.getMessage()
        );

        assertFalse(
                response.getMessage().contains("Database connection failed")
        );

        assertFalse(
                response.getMessage().contains("Ошибка доступа к базе данных")
        );
    }


    @Test
    void shouldMapUnknownExceptionAsInternalError() {
        Request request = createRequest();

        Response response = ExceptionMapper.toResponse(
                request,
                new RuntimeException("Внутренняя техническая информация")
        );

        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals(ErrorCode.INTERNAL, response.getErrorCode());
        assertEquals(
                "Внутренняя ошибка сервера",
                response.getMessage()
        );

        assertNotEquals(
                "Внутренняя техническая информация",
                response.getMessage()
        );
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionMapper.toResponse(
                        null,
                        new ValidationException("Ошибка")
                )
        );
    }

    @Test
    void shouldRejectNullException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionMapper.toResponse(
                        createRequest(),
                        null
                )
        );
    }
}
