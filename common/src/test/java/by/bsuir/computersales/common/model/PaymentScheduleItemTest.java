
package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaymentScheduleItemTest {

    private InstallmentApplication createApplication() {
        return new InstallmentApplication();
    }

    @Test
    void shouldCreatePaymentScheduleItem() {
        InstallmentApplication application = createApplication();

        PaymentScheduleItem item = new PaymentScheduleItem(
                application,
                1,
                LocalDate.of(2026, 12, 20),
                new BigDecimal("150.00")
        );

        assertSame(application, item.getApplication());
        assertEquals(1, item.getPaymentNumber());
        assertEquals(
                LocalDate.of(2026, 12, 20),
                item.getDueDate()
        );
        assertEquals(
                new BigDecimal("150.00"),
                item.getPlannedAmount()
        );
    }

    @Test
    void shouldRejectZeroPaymentNumber() {
        PaymentScheduleItem item = new PaymentScheduleItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> item.setPaymentNumber(0)
        );
    }

    @Test
    void shouldRejectNegativePaymentNumber() {
        PaymentScheduleItem item = new PaymentScheduleItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> item.setPaymentNumber(-1)
        );
    }

    @Test
    void shouldRejectZeroPlannedAmount() {
        PaymentScheduleItem item = new PaymentScheduleItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> item.setPlannedAmount(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldRejectNegativePlannedAmount() {
        PaymentScheduleItem item = new PaymentScheduleItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> item.setPlannedAmount(
                        new BigDecimal("-10.00")
                )
        );
    }

    @Test
    void shouldRejectNullApplication() {
        PaymentScheduleItem item = new PaymentScheduleItem();

        assertThrows(
                NullPointerException.class,
                () -> item.setApplication(null)
        );
    }
}
