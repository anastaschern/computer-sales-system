

package by.bsuir.computersales.server.exception;

import by.bsuir.computersales.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при недостаточном количестве
 * товара на складе.
 */
public class InsufficientStockException extends AppException {

    private static final long serialVersionUID = 1L;

    private final Long productId;
    private final Integer availableQuantity;

    /**
     * Создаёт исключение без дополнительных сведений о товаре.
     */
    public InsufficientStockException(String message) {
        this(message, null, null);
    }

    /**
     * Создаёт исключение с информацией о товаре и остатке на складе.
     */
    public InsufficientStockException(
            String message,
            Long productId,
            Integer availableQuantity) {

        super(ErrorCode.INSUFFICIENT_STOCK, message);

        if (availableQuantity != null && availableQuantity < 0) {
            throw new IllegalArgumentException(
                    "Доступное количество товара не может быть отрицательным"
            );
        }

        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
