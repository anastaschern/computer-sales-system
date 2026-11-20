
package by.bsuir.computersales.common.dto;

import java.io.Serializable;

/**
 * Данные одной товарной позиции при оформлении продажи.
 */
public record SaleItemData(
        Long productId,
        int quantity
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public SaleItemData {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор товара должен быть положительным"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество товара должно быть положительным"
            );
        }
    }
}
