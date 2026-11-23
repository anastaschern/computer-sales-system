
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.DataAccessException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к графику платежей по рассрочке.
 */
public class PaymentScheduleDaoImpl implements PaymentScheduleDao {

    private final DatabaseManager databaseManager;
    private final InstallmentApplicationDao applicationDao;

    public PaymentScheduleDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "DatabaseManager не должен быть null"
        );

        this.applicationDao =
                new InstallmentApplicationDaoImpl(databaseManager);
    }

    /**
     * Возвращает график платежей по заявке.
     */
    @Override
    public List<PaymentScheduleItem> findByApplicationId(
            Long applicationId) {

        validateId(applicationId);

        String sql = """
                SELECT application_id,
                       payment_number,
                       due_date,
                       planned_amount
                FROM payment_schedule
                WHERE application_id = ?
                ORDER BY payment_number
                """;

        List<PaymentScheduleItem> items = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapScheduleItem(resultSet));
                }
            }

            return items;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось получить график платежей по заявке",
                    e
            );
        }
    }

    /**
     * Создаёт одну запись графика платежей.
     */
    @Override
    public void create(PaymentScheduleItem item) {
        validateItem(item);

        String sql = """
                INSERT INTO payment_schedule (
                    application_id,
                    payment_number,
                    due_date,
                    planned_amount
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            fillStatement(statement, item);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось создать запись графика платежей",
                    e
            );
        }
    }


    /**
     * Создаёт несколько записей графика в одной транзакции.
     *
     * @param items список записей графика
     */
    @Override
    public void createAll(List<PaymentScheduleItem> items) {

        validateItems(items);

        if (items.isEmpty()) {
            return;
        }

        databaseManager.executeInTransaction(connection -> {
            createAll(connection, items);
            return null;
        });
    }

    /**
     * Создаёт несколько записей графика,
     * используя переданное соединение.
     *
     * Транзакцией и соединением управляет вызывающий код.
     *
     * @param connection соединение с базой данных
     * @param items список записей графика
     */
    public void createAll(
            Connection connection,
            List<PaymentScheduleItem> items) {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Соединение с базой данных не должно быть null"
            );
        }

        validateItems(items);

        if (items.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO payment_schedule (
                application_id,
                payment_number,
                due_date,
                planned_amount
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (PaymentScheduleItem item : items) {
                fillStatement(statement, item);
                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось сохранить график платежей",
                    exception
            );
        }
    }

    /**
     * Проверяет список записей графика до начала транзакции.
     *
     * @param items список записей
     */
    private void validateItems(List<PaymentScheduleItem> items) {

        if (items == null) {
            throw new IllegalArgumentException(
                    "Список платежей не должен быть null"
            );
        }

        for (PaymentScheduleItem item : items) {
            validateItem(item);
        }
    }


    /**
     * Удаляет график платежей указанной заявки.
     */
    @Override
    public void deleteByApplicationId(Long applicationId) {
        validateId(applicationId);

        String sql = """
                DELETE FROM payment_schedule
                WHERE application_id = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось удалить график платежей",
                    e
            );
        }
    }

    /**
     * Преобразует строку результата SQL-запроса в объект модели.
     */
    private PaymentScheduleItem mapScheduleItem(ResultSet resultSet)
            throws SQLException {

        Long applicationId = resultSet.getLong("application_id");

        Optional<InstallmentApplication> application =
                applicationDao.findById(applicationId);

        InstallmentApplication applicationModel =
                application.orElseThrow(() ->
                        new DataAccessException(
                                "Заявка для записи графика не найдена",
                                new SQLException(
                                        "Application ID: " + applicationId
                                )
                        )
                );

        PaymentScheduleItem item = new PaymentScheduleItem();

        item.setApplication(applicationModel);
        item.setPaymentNumber(
                resultSet.getInt("payment_number")
        );
        item.setDueDate(
                resultSet.getDate("due_date").toLocalDate()
        );
        item.setPlannedAmount(
                resultSet.getBigDecimal("planned_amount")
        );

        return item;
    }

    /**
     * Заполняет параметры подготовленного SQL-запроса.
     */
    private void fillStatement(
            PreparedStatement statement,
            PaymentScheduleItem item) throws SQLException {

        statement.setLong(
                1,
                item.getApplication().getApplicationId()
        );
        statement.setInt(2, item.getPaymentNumber());
        statement.setDate(
                3,
                Date.valueOf(item.getDueDate())
        );
        statement.setBigDecimal(
                4,
                item.getPlannedAmount()
        );
    }

    /**
     * Проверяет корректность записи графика.
     */
    private void validateItem(PaymentScheduleItem item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Запись графика платежей не должна быть null"
            );
        }

        if (item.getApplication().getApplicationId() == null
                || item.getApplication().getApplicationId() <= 0) {

            throw new IllegalArgumentException(
                    "У заявки должен быть корректный идентификатор"
            );
        }
    }

    /**
     * Проверяет идентификатор заявки.
     */
    private void validateId(Long applicationId) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор заявки должен быть положительным"
            );
        }
    }

    /**
     * Отменяет транзакцию при возникновении ошибки.
     */
    private void rollback(Connection connection, Throwable originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }
}
