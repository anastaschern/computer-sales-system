
package by.bsuir.computersales.client.common.dto;

import by.bsuir.computersales.client.common.model.PaymentType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Данные для оформления новой продажи.
 */
public record CreateSaleData(
        Long customerId,
        PaymentType paymentType,
        List<SaleItemData> items
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public CreateSaleData {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор покупателя должен быть положительным"
            );
        }

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Тип оплаты не может быть null"
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Продажа должна содержать хотя бы один товар"
            );
        }

        if (items.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException(
                    "Список товаров не должен содержать null"
            );
        }

        items = new ArrayList<>(items);
    }

    /**
     * Возвращает неизменяемую копию списка товаров.
     */
    @Override
    public List<SaleItemData> items() {
        return List.copyOf(items);
    }
}
