
package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.model.*;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.dao.*;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private static DatabaseManager databaseManager;

    private PaymentDao paymentDao;
    private PaymentScheduleDao paymentScheduleDao;
    private InstallmentApplicationDao applicationDao;
    private SaleDao saleDao;
    private PaymentService paymentService;

    private Sale sale;
    private InstallmentApplication application;
    private User operator;

    private Long applicationId;

    private final List<String> referenceNumbers =
            new ArrayList<>();

    @BeforeAll
    static void setUpDatabase() {
        ServerConfig config =
                ServerConfig.load(Path.of("../server-test.properties"));

        databaseManager = new DatabaseManager(config);
    }

    @AfterAll
    static void closeDatabase() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @BeforeEach
    void setUp() {
        paymentDao = new PaymentDaoImpl(databaseManager);
        paymentScheduleDao =
                new PaymentScheduleDaoImpl(databaseManager);
        applicationDao =
                new InstallmentApplicationDaoImpl(databaseManager);
        saleDao = new SaleDaoImpl(databaseManager);

        paymentService = new PaymentService(
                paymentDao,
                paymentScheduleDao,
                applicationDao,
                saleDao,
                databaseManager
        );

        sale = saleDao.findById(3L)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Тестовая продажа № 3 не найдена"
                        )
                );

        operator = new User(
                2L,
                "manager_test",
                new Role(
                        2L,
                        RoleType.SALES_MANAGER,
                        "Менеджер по продажам"
                ),
                true,
                LocalDateTime.now()
        );

        application = new InstallmentApplication();
        application.setSale(sale);
        application.setTermMonths(1);
        application.setDownPaymentAmount(
                new BigDecimal("300.00")
        );

        applicationId = applicationDao.create(application);

        application = applicationDao.findById(applicationId)
                .orElseThrow();

        application.setStatus(
                InstallmentApplicationStatus.APPROVED
        );
        application.setReviewer(operator);
        application.setDecisionDate(LocalDateTime.now());

        applicationDao.update(application);

        paymentScheduleDao.create(
                new PaymentScheduleItem(
                        application,
                        1,
                        LocalDate.of(2027, 1, 15),
                        new BigDecimal("1200.00")
                )
        );
    }

    @AfterEach
    void cleanUp() {
        try (Connection connection =
                     databaseManager.getConnection()) {

            for (String referenceNumber : referenceNumbers) {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     """
                                     DELETE FROM payments
                                     WHERE reference_number = ?
                                     """
                             )) {
                    statement.setString(1, referenceNumber);
                    statement.executeUpdate();
                }
            }

            if (applicationId != null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     """
                                     DELETE FROM payment_schedule
                                     WHERE application_id = ?
                                     """
                             )) {
                    statement.setLong(1, applicationId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     """
                                     DELETE FROM installment_applications
                                     WHERE application_id = ?
                                     """
                             )) {
                    statement.setLong(1, applicationId);
                    statement.executeUpdate();
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 """
                                 UPDATE sales
                                 SET status = 'REGISTERED'
                                 WHERE sale_id = 3
                                 """
                         )) {
                statement.executeUpdate();
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Не удалось очистить тестовые данные",
                    exception
            );
        } finally {
            referenceNumbers.clear();
            applicationId = null;
        }
    }

    @Test
    void shouldRegisterDownPayment() {
        Payment payment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "300.00",
                null,
                null
        );

        Payment result = paymentService.registerPayment(
                payment,
                operator,
                applicationId
        );

        assertNotNull(result.getPaymentId());
        assertNull(result.getApplication());
        assertNull(result.getPaymentNumber());

        InstallmentApplication updated =
                applicationDao.findById(applicationId)
                        .orElseThrow();

        assertEquals(
                InstallmentApplicationStatus.APPROVED,
                updated.getStatus()
        );
    }
    @Test
    void shouldSupportPartialDownPaymentAndRejectRepeatedPayment() {
        Payment firstPayment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "100.00",
                null,
                null
        );

        Payment firstResult = paymentService.registerPayment(
                firstPayment,
                operator,
                applicationId
        );

        assertNotNull(firstResult.getPaymentId());

        Payment secondPayment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "200.00",
                null,
                null
        );

        Payment secondResult = paymentService.registerPayment(
                secondPayment,
                operator,
                applicationId
        );

        assertNotNull(secondResult.getPaymentId());

        Payment repeatedPayment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "1.00",
                null,
                null
        );

        assertThrows(
                BusinessRuleException.class,
                () -> paymentService.registerPayment(
                        repeatedPayment,
                        operator,
                        applicationId
                )
        );

        List<Payment> payments =
                paymentDao.findBySaleId(sale.getSaleId());

        BigDecimal totalDownPayment = payments.stream()
                .filter(payment ->
                        payment.getPaymentPurpose()
                                == PaymentPurpose.DOWN_PAYMENT
                )
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(
                0,
                totalDownPayment.compareTo(new BigDecimal("300.00"))
        );

        assertTrue(
                payments.stream().noneMatch(payment ->
                        repeatedPayment.getReferenceNumber().equals(
                                payment.getReferenceNumber()
                        )
                )
        );
    }
    @Test
    void shouldRejectSchedulePaymentBeforeFullDownPayment() {
        Payment payment = createPayment(
                PaymentPurpose.INSTALLMENT_PAYMENT,
                "100.00",
                application,
                1
        );

        assertThrows(
                BusinessRuleException.class,
                () -> paymentService.registerPayment(
                        payment,
                        operator
                )
        );

        assertTrue(
                paymentDao.findBySaleId(sale.getSaleId())
                        .stream()
                        .noneMatch(item ->
                                payment.getReferenceNumber().equals(
                                        item.getReferenceNumber()
                                )
                        )
        );
    }

    @Test
    void shouldRejectScheduleOverpayment() {
        Payment downPayment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "300.00",
                null,
                null
        );

        paymentService.registerPayment(
                downPayment,
                operator,
                applicationId
        );

        Payment schedulePayment = createPayment(
                PaymentPurpose.INSTALLMENT_PAYMENT,
                "1200.01",
                application,
                1
        );

        assertThrows(
                BusinessRuleException.class,
                () -> paymentService.registerPayment(
                        schedulePayment,
                        operator
                )
        );
    }

    @Test
    void shouldCompleteInstallmentAfterFullSchedulePayment() {
        Payment downPayment = createPayment(
                PaymentPurpose.DOWN_PAYMENT,
                "300.00",
                null,
                null
        );

        paymentService.registerPayment(
                downPayment,
                operator,
                applicationId
        );

        Payment schedulePayment = createPayment(
                PaymentPurpose.INSTALLMENT_PAYMENT,
                "1200.00",
                application,
                1
        );

        Payment result = paymentService.registerPayment(
                schedulePayment,
                operator
        );

        assertNotNull(result.getPaymentId());

        InstallmentApplication updatedApplication =
                applicationDao.findById(applicationId)
                        .orElseThrow();

        Sale updatedSale = saleDao.findById(sale.getSaleId())
                .orElseThrow();

        assertEquals(
                InstallmentApplicationStatus.COMPLETED,
                updatedApplication.getStatus()
        );

        assertEquals(
                SaleStatus.PAID,
                updatedSale.getStatus()
        );
    }

    private Payment createPayment(
            PaymentPurpose purpose,
            String amount,
            InstallmentApplication paymentApplication,
            Integer paymentNumber
    ) {
        String referenceNumber =
                "TEST-SERVICE-" + UUID.randomUUID();

        referenceNumbers.add(referenceNumber);

        return new Payment(
                null,
                sale,
                paymentApplication,
                paymentNumber,
                LocalDateTime.now(),
                new BigDecimal(amount),
                PaymentMethod.CASH,
                purpose,
                referenceNumber
        );
    }
}
