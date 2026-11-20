
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.model.PaymentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleDataTest {

    @Test
    void shouldCreateSaleItemData() {
        SaleItemData item = new SaleItemData(1L, 2);

        assertEquals(1L, item.productId());
        assertEquals(2, item.quantity());
    }

    @Test
    void shouldRejectInvalidProductId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SaleItemData(0L, 1)
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SaleItemData(1L, 0)
        );
    }

    @Test
    void shouldCreateSaleData() {
        SaleItemData item = new SaleItemData(1L, 2);

        CreateSaleData sale = new CreateSaleData(
                10L,
                PaymentType.FULL_PAYMENT,
                List.of(item)
        );

        assertEquals(10L, sale.customerId());
        assertEquals(PaymentType.FULL_PAYMENT, sale.paymentType());
        assertEquals(1, sale.items().size());
        assertEquals(item, sale.items().get(0));
    }

    @Test
    void shouldRejectInvalidCustomerId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateSaleData(
                        0L,
                        PaymentType.FULL_PAYMENT,
                        List.of(new SaleItemData(1L, 1))
                )
        );
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateSaleData(
                        1L,
                        PaymentType.FULL_PAYMENT,
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullPaymentType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateSaleData(
                        1L,
                        null,
                        List.of(new SaleItemData(1L, 1))
                )
        );
    }

    @Test
    void shouldMakeItemsImmutable() {
        SaleItemData item = new SaleItemData(1L, 1);

        CreateSaleData sale = new CreateSaleData(
                10L,
                PaymentType.FULL_PAYMENT,
                List.of(item)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> sale.items().add(new SaleItemData(2L, 1))
        );
    }
}
