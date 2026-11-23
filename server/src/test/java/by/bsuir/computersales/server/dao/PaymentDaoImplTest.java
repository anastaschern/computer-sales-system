
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.Payment;
import by.bsuir.computersales.common.model.PaymentMethod;
import by.bsuir.computersales.common.model.PaymentPurpose;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.ValidationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDaoImplTest {

    private static DatabaseManager databaseManager;

    private PaymentDao paymentDao;
    private PaymentScheduleDao paymentScheduleDao;
    private InstallmentApplicationDao applicationDao;
    private SaleDao saleDao;

    private Long applicationId;
    private final List<String> referenceNumbers = new ArrayList<>();

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
        paymentScheduleDao = new PaymentScheduleDaoImpl(databaseManager);
        applicationDao = new InstallmentApplicationDaoImpl(databaseManager);
        saleDao = new SaleDaoImpl(databaseManager);

        // Продажа № 3 имеет тип INSTALLMENT.
        Sale sale = saleDao.findById(3L)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Тестовая продажа № 3 не найдена"
                        )
                );

        // Создаём отдельную заявку для текущего теста.
        InstallmentApplication application =
                new InstallmentApplication();

        application.setSale(sale);
        application.setTermMonths(6);

        applicationId = applicationDao.create(application);
    }

    @AfterEach
    void cleanUp() {
        try (Connection connection =
                     databaseManager.getConnection()) {

            // Удаляем платежи, связанные с тестовой заявкой.
            if (applicationId != null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     """
                                     DELETE FROM payments
                                     WHERE application_id = ?
                                     """
                             )) {

                    statement.setLong(1, applicationId);
                    statement.executeUpdate();
                }
            }

            // Удаляем платежи, созданные без заявки.
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

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось очистить тестовые данные",
                    e
            );
        } finally {
            referenceNumbers.clear();
            applicationId = null;
        }
    }

    @Test
    void createShouldSaveFullPayment() {
        Sale sale = getSale(1L);

        Payment payment = createPayment(
                sale,
                null,
                null,
                "1500.00",
                PaymentPurpose.FULL_PAYMENT
        );

        Long paymentId = paymentDao.create(payment);

        assertNotNull(paymentId);

        Payment result = paymentDao.findById(paymentId)
                .orElseThrow();

        assertEquals(paymentId, result.getPaymentId());
        assertEquals(1L, result.getSale().getSaleId());
        assertNull(result.getApplication());
        assertNull(result.getPaymentNumber());
        assertEquals(
                0,
                new BigDecimal("1500.00")
                        .compareTo(result.getAmount())
        );
        assertEquals(
                PaymentPurpose.FULL_PAYMENT,
                result.getPaymentPurpose()
        );
    }

    @Test
    void findByIdShouldReturnEmptyWhenPaymentDoesNotExist() {
        assertTrue(paymentDao.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void findBySaleIdShouldReturnPaymentsForSelectedSale() {
        Sale sale = getSale(1L);

        Payment payment = createPayment(
                sale,
                null,
                null,
                "1200.00",
                PaymentPurpose.FULL_PAYMENT
        );

        paymentDao.create(payment);

        List<Payment> result =
                paymentDao.findBySaleId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(
                result.stream().anyMatch(
                        item -> item.getReferenceNumber()
                                .equals(payment.getReferenceNumber())
                )
        );
    }

    @Test
    void findByApplicationIdShouldReturnInstallmentPayments() {
        Sale sale = getSale(3L);
        InstallmentApplication application = getApplication();

        paymentScheduleDao.create(
                new PaymentScheduleItem(
                        application,
                        1,
                        LocalDate.of(2027, 1, 15),
                        new BigDecimal("500.00")
                )
        );

        Payment payment = createPayment(
                sale,
                application,
                1,
                "500.00",
                PaymentPurpose.INSTALLMENT_PAYMENT
        );

        paymentDao.create(payment);

        List<Payment> result =
                paymentDao.findByApplicationId(applicationId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPaymentNumber());
        assertEquals(
                applicationId,
                result.get(0).getApplication().getApplicationId()
        );
        assertEquals(
                PaymentPurpose.INSTALLMENT_PAYMENT,
                result.get(0).getPaymentPurpose()
        );
    }

    @Test
    void findAllShouldContainCreatedPayment() {
        Sale sale = getSale(1L);

        Payment payment = createPayment(
                sale,
                null,
                null,
                "900.00",
                PaymentPurpose.FULL_PAYMENT
        );

        paymentDao.create(payment);

        List<Payment> result = paymentDao.findAll();

        assertNotNull(result);
        assertTrue(
                result.stream().anyMatch(
                        item -> item.getReferenceNumber()
                                .equals(payment.getReferenceNumber())
                )
        );
    }

    @Test
    void createShouldRejectNullPayment() {
        assertThrows(
                ValidationException.class,
                () -> paymentDao.create(null)
        );
    }

    @Test
    void findByIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findById(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findById(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findById(-1L)
        );
    }

    @Test
    void findBySaleIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findBySaleId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findBySaleId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findBySaleId(-1L)
        );
    }

    @Test
    void findByApplicationIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findByApplicationId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findByApplicationId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentDao.findByApplicationId(-1L)
        );
    }

    private Sale getSale(Long saleId) {
        return saleDao.findById(saleId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Тестовая продажа № "
                                        + saleId
                                        + " не найдена"
                        )
                );
    }

    private InstallmentApplication getApplication() {
        return applicationDao.findById(applicationId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Тестовая заявка не найдена"
                        )
                );
    }

    private Payment createPayment(
            Sale sale,
            InstallmentApplication application,
            Integer paymentNumber,
            String amount,
            PaymentPurpose purpose) {

        String referenceNumber =
                "TEST-PAYMENT-" + java.util.UUID.randomUUID();

        referenceNumbers.add(referenceNumber);

        return new Payment(
                null,
                sale,
                application,
                paymentNumber,
                LocalDateTime.now(),
                new BigDecimal(amount),
                PaymentMethod.CASH,
                purpose,
                referenceNumber
        );
    }
}
