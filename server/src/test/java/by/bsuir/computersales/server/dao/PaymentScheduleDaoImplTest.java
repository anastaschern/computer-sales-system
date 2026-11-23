
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.DataAccessException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentScheduleDaoImplTest {

    private static DatabaseManager databaseManager;

    private PaymentScheduleDao paymentScheduleDao;
    private InstallmentApplicationDao applicationDao;
    private SaleDao saleDao;

    private Long applicationId;

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
        paymentScheduleDao =
                new PaymentScheduleDaoImpl(databaseManager);

        applicationDao =
                new InstallmentApplicationDaoImpl(databaseManager);

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
        if (applicationId == null) {
            return;
        }

        String deleteScheduleSql = """
                DELETE FROM payment_schedule
                WHERE application_id = ?
                """;

        String deleteApplicationSql = """
                DELETE FROM installment_applications
                WHERE application_id = ?
                """;

        try (Connection connection =
                     databaseManager.getConnection()) {

            try (PreparedStatement statement =
                         connection.prepareStatement(deleteScheduleSql)) {

                statement.setLong(1, applicationId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(deleteApplicationSql)) {

                statement.setLong(1, applicationId);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось очистить тестовые данные",
                    e
            );
        }
    }

    @Test
    void findByApplicationIdShouldReturnEmptyListWhenScheduleDoesNotExist() {
        List<PaymentScheduleItem> result =
                paymentScheduleDao.findByApplicationId(applicationId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createShouldSaveScheduleItem() {
        PaymentScheduleItem item = createItem(
                1,
                LocalDate.of(2027, 1, 15),
                "500.00"
        );

        paymentScheduleDao.create(item);

        List<PaymentScheduleItem> result =
                paymentScheduleDao.findByApplicationId(applicationId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPaymentNumber());
        assertEquals(
                LocalDate.of(2027, 1, 15),
                result.get(0).getDueDate()
        );
        assertEquals(
                0,
                new BigDecimal("500.00")
                        .compareTo(result.get(0).getPlannedAmount())
        );
    }

    @Test
    void createAllShouldSaveAllScheduleItems() {
        List<PaymentScheduleItem> items = List.of(
                createItem(1, LocalDate.of(2027, 1, 15), "500.00"),
                createItem(2, LocalDate.of(2027, 2, 15), "500.00"),
                createItem(3, LocalDate.of(2027, 3, 15), "500.00")
        );

        paymentScheduleDao.createAll(items);

        List<PaymentScheduleItem> result =
                paymentScheduleDao.findByApplicationId(applicationId);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getPaymentNumber());
        assertEquals(2, result.get(1).getPaymentNumber());
        assertEquals(3, result.get(2).getPaymentNumber());
    }

    @Test
    void createAllShouldRollbackWhenDuplicatePaymentNumberExists() {
        List<PaymentScheduleItem> items = List.of(
                createItem(1, LocalDate.of(2027, 1, 15), "500.00"),
                createItem(1, LocalDate.of(2027, 2, 15), "500.00")
        );

        assertThrows(
                DataAccessException.class,
                () -> paymentScheduleDao.createAll(items)
        );

        List<PaymentScheduleItem> result =
                paymentScheduleDao.findByApplicationId(applicationId);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByApplicationIdShouldDeleteOnlySelectedSchedule() {
        paymentScheduleDao.createAll(List.of(
                createItem(1, LocalDate.of(2027, 1, 15), "500.00"),
                createItem(2, LocalDate.of(2027, 2, 15), "500.00")
        ));

        paymentScheduleDao.deleteByApplicationId(applicationId);

        List<PaymentScheduleItem> result =
                paymentScheduleDao.findByApplicationId(applicationId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByApplicationIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.findByApplicationId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.findByApplicationId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.findByApplicationId(-1L)
        );
    }

    @Test
    void deleteByApplicationIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.deleteByApplicationId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.deleteByApplicationId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.deleteByApplicationId(-1L)
        );
    }

    @Test
    void createShouldRejectNullItem() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.create(null)
        );
    }

    @Test
    void createAllShouldRejectNullList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentScheduleDao.createAll(null)
        );
    }

    @Test
    void createAllShouldAcceptEmptyList() {
        assertDoesNotThrow(
                () -> paymentScheduleDao.createAll(List.of())
        );
    }

    private PaymentScheduleItem createItem(
            int paymentNumber,
            LocalDate dueDate,
            String amount) {

        return new PaymentScheduleItem(
                getApplication(),
                paymentNumber,
                dueDate,
                new BigDecimal(amount)
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
}
