package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.model.Customer;
import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.PaymentType;
import by.bsuir.computersales.common.model.Laptop;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.common.model.SaleItem;
import by.bsuir.computersales.common.model.SaleStatus;
import by.bsuir.computersales.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleCalculatorTest {

    private ScheduleCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ScheduleCalculator();
    }

    @Test
    void shouldCalculateAnnuityScheduleWithTwelvePercentRate() {
        InstallmentApplication application =
                createApplication("2500.00", "500.00", "12", 12);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 15)
        );

        assertEquals(12, schedule.size());

        for (int i = 0; i < 11; i++) {
            assertEquals(
                    new BigDecimal("177.70"),
                    schedule.get(i).getPlannedAmount()
            );
        }

        assertEquals(
                new BigDecimal("177.67"),
                schedule.get(11).getPlannedAmount()
        );

        assertEquals(
                new BigDecimal("2132.37"),
                calculateTotal(schedule)
        );
    }

    @Test
    void shouldCalculateScheduleWithoutInterest() {
        InstallmentApplication application =
                createApplication("2500.00", "500.00", "0", 6);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 15)
        );

        assertEquals(6, schedule.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(
                    new BigDecimal("333.33"),
                    schedule.get(i).getPlannedAmount()
            );
        }

        assertEquals(
                new BigDecimal("333.35"),
                schedule.get(5).getPlannedAmount()
        );

        assertEquals(
                new BigDecimal("2000.00"),
                calculateTotal(schedule)
        );
    }

    @Test
    void shouldCalculateScheduleForOneMonth() {
        InstallmentApplication application =
                createApplication("2500.00", "500.00", "12", 1);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 15)
        );

        assertEquals(1, schedule.size());

        assertEquals(
                new BigDecimal("2020.00"),
                schedule.get(0).getPlannedAmount()
        );

        assertEquals(
                LocalDate.of(2026, 2, 15),
                schedule.get(0).getDueDate()
        );
    }

    @Test
    void shouldCalculateScheduleForTwentyFourMonths() {
        InstallmentApplication application =
                createApplication("2500.00", "500.00", "12", 24);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 15)
        );

        assertEquals(24, schedule.size());

        assertEquals(1, schedule.get(0).getPaymentNumber());
        assertEquals(24, schedule.get(23).getPaymentNumber());

        assertTrue(
                schedule.stream().allMatch(
                        item -> item.getPlannedAmount().signum() > 0
                )
        );

        assertEquals(
                LocalDate.of(2028, 1, 15),
                schedule.get(23).getDueDate()
        );
    }

    @Test
    void shouldCorrectLastPaymentAfterRounding() {
        InstallmentApplication application =
                createApplication("1000.00", "0.00", "0", 3);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 10)
        );

        assertEquals(
                new BigDecimal("333.33"),
                schedule.get(0).getPlannedAmount()
        );

        assertEquals(
                new BigDecimal("333.33"),
                schedule.get(1).getPlannedAmount()
        );

        assertEquals(
                new BigDecimal("333.34"),
                schedule.get(2).getPlannedAmount()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                calculateTotal(schedule)
        );
    }

    @Test
    void shouldSetMonthlyDueDatesFromApprovalDate() {
        InstallmentApplication application =
                createApplication("1000.00", "0.00", "0", 3);

        List<PaymentScheduleItem> schedule = calculator.calculate(
                application,
                LocalDate.of(2026, 1, 31)
        );

        assertEquals(
                LocalDate.of(2026, 2, 28),
                schedule.get(0).getDueDate()
        );

        assertEquals(
                LocalDate.of(2026, 3, 31),
                schedule.get(1).getDueDate()
        );

        assertEquals(
                LocalDate.of(2026, 4, 30),
                schedule.get(2).getDueDate()
        );
    }

    @Test
    void shouldRejectNullApplication() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        null,
                        LocalDate.of(2026, 1, 15)
                )
        );
    }

    @Test
    void shouldRejectNullApprovalDate() {
        InstallmentApplication application =
                createApplication("1000.00", "0.00", "0", 3);

        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(application, null)
        );
    }

    @Test
    void shouldRejectApplicationWithoutIdentifier() {
        InstallmentApplication application =
                createApplication("1000.00", "0.00", "0", 3);

        application.setApplicationId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        application,
                        LocalDate.of(2026, 1, 15)
                )
        );
    }

    @Test
    void shouldRejectDownPaymentEqualToSaleAmount() {
        InstallmentApplication application =
                createApplication("1000.00", "1000.00", "0", 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        application,
                        LocalDate.of(2026, 1, 15)
                )
        );
    }

    private InstallmentApplication createApplication(
            String totalAmount,
            String downPayment,
            String interestRate,
            int termMonths) {

        Sale sale = new Sale();

        sale.setCustomer(new Customer());
        sale.setUser(new User());
        sale.setSaleDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setPaymentType(PaymentType.INSTALLMENT);

        sale.setItems(
                List.of(
                        new SaleItem(
                                1,
                                new Laptop(
                                        1L,
                                        null,
                                        "Test",
                                        "Model",
                                        "Test laptop",
                                        "Test specification",
                                        new BigDecimal(totalAmount),
                                        10,
                                        true
                                ),
                                1,
                                new BigDecimal(totalAmount)
                        )
                )
        );

        InstallmentApplication application =
                new InstallmentApplication();

        application.setApplicationId(1L);
        application.setSale(sale);
        application.setTermMonths(termMonths);
        application.setDownPaymentAmount(
                new BigDecimal(downPayment)
        );
        application.setInterestRate(
                new BigDecimal(interestRate)
        );

        return application;
    }

    private BigDecimal calculateTotal(
            List<PaymentScheduleItem> schedule) {

        return schedule.stream()
                .map(PaymentScheduleItem::getPlannedAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}