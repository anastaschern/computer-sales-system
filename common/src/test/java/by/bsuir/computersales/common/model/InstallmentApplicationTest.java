
package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentApplicationTest {

    private Sale createSale() {
        Customer customer = new Customer(
                1L, "Иванов Иван", "+375291234567",
                "ivan@example.com", null
        );

        User user = new User(
                1L, "manager", null, true, LocalDateTime.now()
        );

        return new Sale(
                1L,
                customer,
                user,
                LocalDateTime.now(),
                SaleStatus.REGISTERED,
                PaymentType.INSTALLMENT,
                List.of()
        );
    }

    @Test
    void shouldCreateApplication() {
        LocalDateTime applicationDate = LocalDateTime.of(
                2026, 11, 20, 10, 0
        );

        InstallmentApplication application =
                new InstallmentApplication(
                        1L,
                        createSale(),
                        null,
                        applicationDate,
                        null,
                        12,
                        new BigDecimal("100.00"),
                        new BigDecimal("5.00"),
                        InstallmentApplicationStatus.CREATED,
                        null
                );

        assertEquals(1L, application.getApplicationId());
        assertEquals(12, application.getTermMonths());
        assertEquals(new BigDecimal("100.00"),
                application.getDownPaymentAmount());
        assertEquals(new BigDecimal("5.00"),
                application.getInterestRate());
        assertEquals(InstallmentApplicationStatus.CREATED,
                application.getStatus());
        assertNull(application.getDecisionDate());
    }

    @Test
    void shouldRejectNonPositiveTerm() {
        InstallmentApplication application =
                new InstallmentApplication();

        assertThrows(
                IllegalArgumentException.class,
                () -> application.setTermMonths(0)
        );
    }

    @Test
    void shouldRejectNegativeDownPayment() {
        InstallmentApplication application =
                new InstallmentApplication();

        assertThrows(
                IllegalArgumentException.class,
                () -> application.setDownPaymentAmount(
                        new BigDecimal("-1.00")
                )
        );
    }

    @Test
    void shouldRejectNegativeInterestRate() {
        InstallmentApplication application =
                new InstallmentApplication();

        assertThrows(
                IllegalArgumentException.class,
                () -> application.setInterestRate(
                        new BigDecimal("-1.00")
                )
        );
    }

    @Test
    void shouldRejectDecisionDateBeforeApplicationDate() {
        InstallmentApplication application =
                new InstallmentApplication();

        application.setApplicationDate(
                LocalDateTime.of(2026, 11, 20, 12, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> application.setDecisionDate(
                        LocalDateTime.of(2026, 11, 20, 11, 0)
                )
        );
    }

    @Test
    void shouldRejectRejectedApplicationWithoutReason() {
        InstallmentApplication application =
                new InstallmentApplication();

        application.setStatus(InstallmentApplicationStatus.REJECTED);

        assertThrows(
                IllegalArgumentException.class,
                () -> application.setDecisionReason(" ")
        );
    }
}
