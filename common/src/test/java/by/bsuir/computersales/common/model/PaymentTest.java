
package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    private Sale createSale(Long saleId) {
        Customer customer = new Customer(
                1L, "Иванов Иван", "+375291234567",
                "ivan@example.com", null
        );

        User user = new User(
                1L, "manager", null, true, LocalDateTime.now()
        );

        return new Sale(
                saleId,
                customer,
                user,
                LocalDateTime.now(),
                SaleStatus.REGISTERED,
                PaymentType.INSTALLMENT,
                List.of()
        );
    }

    private InstallmentApplication createApplication(Sale sale) {
        return new InstallmentApplication(
                1L,
                sale,
                null,
                LocalDateTime.now(),
                null,
                12,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                InstallmentApplicationStatus.APPROVED,
                null
        );
    }

    @Test
    void shouldCreateFullPayment() {
        Sale sale = createSale(1L);

        Payment payment = new Payment(
                1L,
                sale,
                null,
                null,
                LocalDateTime.now(),
                new BigDecimal("500.00"),
                PaymentMethod.CARD,
                PaymentPurpose.FULL_PAYMENT
        );

        payment.validatePaymentReferences();

        assertEquals(1L, payment.getPaymentId());
        assertSame(sale, payment.getSale());
        assertEquals(new BigDecimal("500.00"), payment.getAmount());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals(PaymentPurpose.FULL_PAYMENT,
                payment.getPaymentPurpose());
    }

    @Test
    void shouldCreateInstallmentPayment() {
        Sale sale = createSale(1L);
        InstallmentApplication application = createApplication(sale);

        Payment payment = new Payment(
                1L,
                sale,
                application,
                1,
                LocalDateTime.now(),
                new BigDecimal("150.00"),
                PaymentMethod.TRANSFER,
                PaymentPurpose.INSTALLMENT_PAYMENT
        );

        assertDoesNotThrow(payment::validatePaymentReferences);
    }

    @Test
    void shouldRejectZeroAmount() {
        Payment payment = new Payment();

        assertThrows(
                IllegalArgumentException.class,
                () -> payment.setAmount(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        Payment payment = new Payment();

        assertThrows(
                IllegalArgumentException.class,
                () -> payment.setAmount(new BigDecimal("-1.00"))
        );
    }

    @Test
    void shouldRejectInstallmentPaymentWithoutApplication() {
        Payment payment = new Payment(
                1L,
                createSale(1L),
                null,
                null,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                PaymentMethod.CASH,
                PaymentPurpose.INSTALLMENT_PAYMENT
        );

        assertThrows(
                IllegalArgumentException.class,
                payment::validatePaymentReferences
        );
    }

    @Test
    void shouldRejectPaymentWithApplicationWithoutPaymentNumber() {
        Sale sale = createSale(1L);
        InstallmentApplication application = createApplication(sale);

        Payment payment = new Payment(
                1L,
                sale,
                application,
                null,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                PaymentMethod.CARD,
                PaymentPurpose.INSTALLMENT_PAYMENT
        );

        assertThrows(
                IllegalArgumentException.class,
                payment::validatePaymentReferences
        );
    }

    @Test
    void shouldRejectApplicationFromAnotherSale() {
        Sale firstSale = createSale(1L);
        Sale secondSale = createSale(2L);

        InstallmentApplication application =
                createApplication(firstSale);

        Payment payment = new Payment(
                1L,
                secondSale,
                application,
                1,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                PaymentMethod.CARD,
                PaymentPurpose.INSTALLMENT_PAYMENT
        );

        assertThrows(
                IllegalArgumentException.class,
                payment::validatePaymentReferences
        );
    }
}
