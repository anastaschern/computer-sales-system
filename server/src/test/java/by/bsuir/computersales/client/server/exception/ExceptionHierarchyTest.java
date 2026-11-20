
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHierarchyTest {

    @Test
    void shouldRejectNullErrorCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AppException(null, "Ошибка")
        );
    }

    @Test
    void shouldRejectInvalidAuthenticationErrorCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AuthenticationException(
                        ErrorCode.VALIDATION,
                        "Некорректные данные"
                )
        );
    }

    @Test
    void shouldPreserveOriginalCauseInDataAccessException() {
        RuntimeException originalCause =
                new RuntimeException("Ошибка подключения к БД");

        DataAccessException exception = new DataAccessException(
                "Не удалось выполнить запрос",
                originalCause
        );

        assertEquals(ErrorCode.INTERNAL, exception.getErrorCode());
        assertSame(originalCause, exception.getCause());
    }

    @Test
    void shouldStoreProductIdAndAvailableQuantity() {
        InsufficientStockException exception =
                new InsufficientStockException(
                        "Недостаточно товара",
                        15L,
                        2
                );

        assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        assertEquals(15L, exception.getProductId());
        assertEquals(2, exception.getAvailableQuantity());
    }

    @Test
    void shouldRejectNegativeAvailableQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new InsufficientStockException(
                        "Недостаточно товара",
                        15L,
                        -2
                )
        );
    }
}

