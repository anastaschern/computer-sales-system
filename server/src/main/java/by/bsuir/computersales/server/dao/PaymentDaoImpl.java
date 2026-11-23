
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.Payment;
import by.bsuir.computersales.common.model.PaymentMethod;
import by.bsuir.computersales.common.model.PaymentPurpose;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import by.bsuir.computersales.server.exception.DataAccessException;
import by.bsuir.computersales.server.exception.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к данным о фактически внесённых платежах.
 */
public class PaymentDaoImpl implements PaymentDao {

    private static final String SELECT_PAYMENTS_SQL = """
            SELECT payment_id,
                   sale_id,
                   application_id,
                   payment_number,
                   payment_date,
                   amount,
                   payment_method,
                   payment_purpose,
                   reference_number
            FROM payments
            """;

    private static final String INSERT_PAYMENT_SQL = """
            INSERT INTO payments (
                sale_id,
                application_id,
                payment_number,
                payment_date,
                amount,
                payment_method,
                payment_purpose,
                reference_number
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING payment_id
            """;

    private final DatabaseManager databaseManager;
    private final SaleDao saleDao;
    private final InstallmentApplicationDao applicationDao;

    public PaymentDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );

        this.saleDao = new SaleDaoImpl(databaseManager);
        this.applicationDao =
                new InstallmentApplicationDaoImpl(databaseManager);
    }

    @Override
    public List<Payment> findAll() {
        return executeListQuery(
                SELECT_PAYMENTS_SQL + " ORDER BY payment_date DESC, payment_id DESC",
                null,
                "получить список платежей"
        );
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        validateId(paymentId, "платежа");

        List<Payment> payments = executeListQuery(
                SELECT_PAYMENTS_SQL + " WHERE payment_id = ?",
                paymentId,
                "найти платёж по идентификатору"
        );

        return payments.stream().findFirst();
    }

    @Override
    public List<Payment> findBySaleId(Long saleId) {
        validateId(saleId, "продажи");

        return executeListQuery(
                SELECT_PAYMENTS_SQL
                        + " WHERE sale_id = ?"
                        + " ORDER BY payment_date, payment_id",
                saleId,
                "получить платежи продажи"
        );
    }

    @Override
    public List<Payment> findByApplicationId(Long applicationId) {
        validateId(applicationId, "заявки");

        return executeListQuery(
                SELECT_PAYMENTS_SQL
                        + " WHERE application_id = ?"
                        + " ORDER BY payment_number, payment_date",
                applicationId,
                "получить платежи по заявке на рассрочку"
        );
    }

    /**
     * Регистрирует новый платёж.
     */
    @Override
    public Long create(Payment payment) {
        if (payment == null) {
            throw new ValidationException(
                    "Данные платежа не должны быть null"
            );
        }

        if (payment.getPaymentId() != null) {
            throw new ValidationException(
                    "При создании платежа его идентификатор должен быть null"
            );
        }

        if (payment.getSale() == null
                || payment.getSale().getSaleId() == null) {
            throw new ValidationException(
                    "Для регистрации платежа необходимо указать продажу"
            );
        }

        payment.validatePaymentReferences();

        Long saleId = payment.getSale().getSaleId();
        validateId(saleId, "продажи");

        // Проверяем существование продажи.
        Sale sale = saleDao.findById(saleId)
                .orElseThrow(() -> new ValidationException(
                        "Продажа с идентификатором "
                                + saleId + " не найдена"
                ));

        Long applicationId = null;
        Integer paymentNumber = null;

        if (payment.getApplication() != null) {
            applicationId =
                    payment.getApplication().getApplicationId();

            validateId(applicationId, "заявки на рассрочку");

            Long currentApplicationId = applicationId;

            InstallmentApplication application =
                    applicationDao.findById(currentApplicationId)
                            .orElseThrow(() ->
                                    new ValidationException(
                                            "Заявка с идентификатором "
                                                    + currentApplicationId
                                                    + " не найдена"
                                    )
                            );
            if (!Objects.equals(
                    application.getSale().getSaleId(),
                    saleId)) {
                throw new BusinessRuleException(
                        "Заявка на рассрочку относится к другой продаже"
                );
            }

            paymentNumber = payment.getPaymentNumber();

            validateScheduleItem(applicationId, paymentNumber);
        }

        // Проверяем соответствие назначения платежа типу продажи.
        if (payment.getPaymentPurpose()
                == PaymentPurpose.INSTALLMENT_PAYMENT
                && sale.getPaymentType()
                != by.bsuir.computersales.common.model.PaymentType.INSTALLMENT) {
            throw new BusinessRuleException(
                    "Платёж по графику рассрочки возможен "
                            + "только для продажи с типом INSTALLMENT"
            );
        }

        String sql = INSERT_PAYMENT_SQL;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            if (applicationId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setLong(2, applicationId);
                statement.setInt(3, paymentNumber);
            }

            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(payment.getPaymentDate())
            );

            statement.setBigDecimal(5, payment.getAmount());
            statement.setString(6, payment.getPaymentMethod().name());
            statement.setString(7, payment.getPaymentPurpose().name());
            statement.setString(8, payment.getReferenceNumber());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("payment_id");
                }

                throw new DataAccessException(
                        "База данных не вернула идентификатор платежа",
                        new SQLException(
                                "INSERT не вернул payment_id"
                        )
                );
            }

        } catch (SQLException exception) {
            if ("23503".equals(exception.getSQLState())) {
                throw new BusinessRuleException(
                        "Нарушена связь платежа с продажей, "
                                + "заявкой или графиком рассрочки"
                );
            }

            if ("23505".equals(exception.getSQLState())) {
                throw new BusinessRuleException(
                        "Платёж с указанными уникальными данными "
                                + "уже существует"
                );
            }

            throw new DataAccessException(
                    "Не удалось зарегистрировать платёж",
                    exception
            );
        }
    }

    /**
     * Проверяет наличие номера платежа в графике рассрочки.
     */
    private void validateScheduleItem(
            Long applicationId,
            Integer paymentNumber) {

        String sql = """
                SELECT 1
                FROM payment_schedule
                WHERE application_id = ?
                  AND payment_number = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);
            statement.setInt(2, paymentNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new BusinessRuleException(
                            "Указанный номер платежа отсутствует "
                                    + "в графике рассрочки"
                    );
                }
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось проверить график рассрочки",
                    exception
            );
        }
    }

    /**
     * Выполняет запрос и преобразует результат в список платежей.
     */
    private List<Payment> executeListQuery(
            String sql,
            Long parameter,
            String operation) {

        List<Payment> payments = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setLong(1, parameter);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }

            return payments;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось " + operation,
                    exception
            );
        }
    }

    /**
     * Преобразует строку результата запроса в объект Payment.
     */
    private Payment mapPayment(ResultSet resultSet)
            throws SQLException {

        long saleId = resultSet.getLong("sale_id");

        Sale sale = saleDao.findById(saleId)
                .orElseThrow(() -> new DataAccessException(
                        "Не найдена продажа с идентификатором "
                                + saleId,
                        new SQLException(
                                "Нарушена связь платежа с продажей"
                        )
                ));

        long applicationId =
                resultSet.getLong("application_id");

        InstallmentApplication application = null;

        if (!resultSet.wasNull()) {
            application = applicationDao.findById(applicationId)
                    .orElseThrow(() -> new DataAccessException(
                            "Не найдена заявка с идентификатором "
                                    + applicationId,
                            new SQLException(
                                    "Нарушена связь платежа с заявкой"
                            )
                    ));
        }

        int paymentNumberValue =
                resultSet.getInt("payment_number");

        Integer paymentNumber =
                resultSet.wasNull() ? null : paymentNumberValue;

        Timestamp paymentTimestamp =
                resultSet.getTimestamp("payment_date");

        Payment payment = new Payment(
                resultSet.getLong("payment_id"),
                sale,
                application,
                paymentNumber,
                paymentTimestamp.toLocalDateTime(),
                resultSet.getBigDecimal("amount"),
                PaymentMethod.valueOf(
                        resultSet.getString("payment_method")
                ),
                PaymentPurpose.valueOf(
                        resultSet.getString("payment_purpose")
                ),
                resultSet.getString("reference_number")
        );

        payment.validatePaymentReferences();

        return payment;
    }

    /**
     * Проверяет корректность идентификатора.
     */
    private void validateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор " + entityName
                            + " должен быть положительным"
            );
        }
    }
}
