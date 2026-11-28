
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.InstallmentApplicationStatus;
import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.server.exception.DataAccessException;
import by.bsuir.computersales.common.model.PaymentType;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import by.bsuir.computersales.server.exception.ValidationException;

import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * JDBC-реализация доступа к данным о заявках на рассрочку.
 */
public class InstallmentApplicationDaoImpl
        implements InstallmentApplicationDao {

    private static final String SELECT_APPLICATIONS_SQL = """
        SELECT
            ia.application_id,
            ia.sale_id,
            ia.reviewer_id,
            ia.application_date,
            ia.decision_date,
            ia.term_months,
            ia.down_payment_amount,
            ia.interest_rate,
            ia.status AS application_status,
            ia.decision_reason,

            ru.user_id AS reviewer_user_id,
            ru.login AS reviewer_login,
            ru.is_active AS reviewer_is_active,
            ru.created_at AS reviewer_created_at,

            rr.role_id AS reviewer_role_id,
            rr.role_name AS reviewer_role_name,
            rr.description AS reviewer_role_description

        FROM installment_applications ia
        LEFT JOIN users ru ON ia.reviewer_id = ru.user_id
        LEFT JOIN roles rr ON ru.role_id = rr.role_id
        """;
    private final DatabaseManager databaseManager;
    private final SaleDao saleDao;

    public InstallmentApplicationDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );

        this.saleDao = new SaleDaoImpl(databaseManager);
    }
    /**
     * Преобразует данные проверяющего сотрудника из ResultSet в User.
     *
     * @param resultSet результат SQL-запроса
     * @return проверяющий сотрудник или null, если он не назначен
     * @throws SQLException при ошибке чтения данных
     */
    private User mapReviewer(ResultSet resultSet) throws SQLException {
        long reviewerId = resultSet.getLong("reviewer_user_id");

        if (resultSet.wasNull()) {
            return null;
        }

        Role role = new Role(
                resultSet.getLong("reviewer_role_id"),
                RoleType.valueOf(
                        resultSet.getString("reviewer_role_name")
                ),
                resultSet.getString("reviewer_role_description")
        );

        return new User(
                reviewerId,
                resultSet.getString("reviewer_login"),
                role,
                resultSet.getBoolean("reviewer_is_active"),
                resultSet.getTimestamp("reviewer_created_at")
                        .toLocalDateTime()
        );
    }

    /**
     * Преобразует строку результата SQL-запроса в заявку на рассрочку.
     *
     * @param resultSet результат SQL-запроса
     * @return объект заявки
     * @throws SQLException при ошибке чтения данных
     */
    private InstallmentApplication mapApplication(ResultSet resultSet)
            throws SQLException {

        long saleId = resultSet.getLong("sale_id");

        Sale sale = saleDao.findById(saleId)
                .orElseThrow(() -> new DataAccessException(
                        "Не найдена продажа с идентификатором " + saleId,
                        new SQLException("Нарушена связь заявки с продажей")
                ));

        java.sql.Timestamp decisionTimestamp =
                resultSet.getTimestamp("decision_date");

        return new InstallmentApplication(
                resultSet.getLong("application_id"),
                sale,
                mapReviewer(resultSet),
                resultSet.getTimestamp("application_date")
                        .toLocalDateTime(),
                decisionTimestamp == null
                        ? null
                        : decisionTimestamp.toLocalDateTime(),
                resultSet.getInt("term_months"),
                resultSet.getBigDecimal("down_payment_amount"),
                resultSet.getBigDecimal("interest_rate"),
                InstallmentApplicationStatus.valueOf(
                        resultSet.getString("application_status")
                ),
                resultSet.getString("decision_reason")
        );
    }

    /**
     * Выполняет SQL-запрос и преобразует результат в список заявок.
     *
     * @param sql SQL-запрос
     * @param parameter необязательный параметр запроса
     * @param operation описание выполняемой операции
     * @return список заявок
     */
    private List<InstallmentApplication> executeListQuery(
            String sql,
            Long parameter,
            String operation) {

        List<InstallmentApplication> applications = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setLong(1, parameter);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    applications.add(mapApplication(resultSet));
                }
            }

            return applications;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось " + operation,
                    exception
            );
        }
    }
    @Override
    public List<InstallmentApplication> findAll() {
        String sql = SELECT_APPLICATIONS_SQL
                + " ORDER BY ia.application_date DESC, ia.application_id DESC";

        return executeListQuery(
                sql,
                null,
                "получить список заявок на рассрочку"
        );
    }

    @Override
    public Optional<InstallmentApplication> findById(Long applicationId) {
        validateId(applicationId, "заявки на рассрочку");

        String sql = SELECT_APPLICATIONS_SQL
                + " WHERE ia.application_id = ?";

        List<InstallmentApplication> applications = executeListQuery(
                sql,
                applicationId,
                "найти заявку на рассрочку по идентификатору"
        );

        return applications.stream().findFirst();
    }

    @Override
    public Optional<InstallmentApplication> findBySaleId(Long saleId) {
        validateId(saleId, "продажи");

        String sql = SELECT_APPLICATIONS_SQL
                + " WHERE ia.sale_id = ?";

        List<InstallmentApplication> applications = executeListQuery(
                sql,
                saleId,
                "найти заявку на рассрочку по продаже"
        );

        return applications.stream().findFirst();
    }

    @Override
    public List<InstallmentApplication> findByStatus(
            InstallmentApplicationStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Статус заявки не должен быть null"
            );
        }

        String sql = SELECT_APPLICATIONS_SQL
                + " WHERE ia.status = ?"
                + " ORDER BY ia.application_date DESC, ia.application_id DESC";

        List<InstallmentApplication> applications = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    applications.add(mapApplication(resultSet));
                }
            }

            return applications;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить заявки по статусу",
                    exception
            );
        }
    }

    @Override
    public List<InstallmentApplication> findByReviewerId(Long reviewerId) {
        validateId(reviewerId, "сотрудника");

        String sql = SELECT_APPLICATIONS_SQL
                + " WHERE ia.reviewer_id = ?"
                + " ORDER BY ia.application_date DESC, ia.application_id DESC";

        return executeListQuery(
                sql,
                reviewerId,
                "получить заявки сотрудника"
        );
    }

    @Override
    public Long create(InstallmentApplication application) {
        if (application == null) {
            throw new ValidationException(
                    "Заявка на рассрочку не должна быть null"
            );
        }

        if (application.getApplicationId() != null) {
            throw new ValidationException(
                    "При создании заявки её идентификатор должен быть null"
            );
        }

        Sale applicationSale = application.getSale();

        if (applicationSale == null || applicationSale.getSaleId() == null) {
            throw new ValidationException(
                    "Для создания заявки необходимо указать продажу"
            );
        }

        Long saleId = applicationSale.getSaleId();
        validateId(saleId, "продажи");

        Sale sale = saleDao.findById(saleId)
                .orElseThrow(() -> new ValidationException(
                        "Продажа с идентификатором " + saleId + " не найдена"
                ));

        if (sale.getPaymentType() != PaymentType.INSTALLMENT) {
            throw new BusinessRuleException(
                    "Заявку на рассрочку можно создать только для продажи "
                            + "с типом оплаты INSTALLMENT"
            );
        }

        Long reviewerId = null;

        if (application.getReviewer() != null) {
            reviewerId = application.getReviewer().getUserId();
            validateId(reviewerId, "проверяющего сотрудника");
        }

        String sql = """
            INSERT INTO installment_applications (
                sale_id,
                reviewer_id,
                application_date,
                decision_date,
                term_months,
                down_payment_amount,
                interest_rate,
                status,
                decision_reason
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING application_id
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            if (reviewerId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, reviewerId);
            }

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(application.getApplicationDate())
            );

            if (application.getDecisionDate() == null) {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(
                        4,
                        Timestamp.valueOf(application.getDecisionDate())
                );
            }

            statement.setInt(5, application.getTermMonths());
            statement.setBigDecimal(6, application.getDownPaymentAmount());
            statement.setBigDecimal(7, application.getInterestRate());
            statement.setString(8, application.getStatus().name());
            statement.setString(9, application.getDecisionReason());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("application_id");
                }

                throw new DataAccessException(
                        "Не удалось получить идентификатор созданной заявки",
                        new SQLException("INSERT не вернул application_id")
                );
            }

        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new BusinessRuleException(
                        "Для этой продажи уже существует заявка на рассрочку"
                );
            }

            throw new DataAccessException(
                    "Не удалось создать заявку на рассрочку",
                    exception
            );
        }
    }

    @Override
    public void update(InstallmentApplication application) {
        if (application == null) {
            throw new ValidationException(
                    "Заявка на рассрочку не должна быть null"
            );
        }

        try (Connection connection = databaseManager.getConnection()) {
            update(connection, application);
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось обновить заявку на рассрочку",
                    exception
            );
        }
    }

    /**
     * Обновляет заявку, используя переданное соединение.
     *
     * Соединением и транзакцией управляет вызывающий код.
     *
     * @param connection соединение с базой данных
     * @param application обновляемая заявка
     */
    public void update(
            Connection connection,
            InstallmentApplication application) {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Соединение с базой данных не должно быть null"
            );
        }

        if (application == null) {
            throw new ValidationException(
                    "Заявка на рассрочку не должна быть null"
            );
        }

        Long applicationId = application.getApplicationId();
        validateId(applicationId, "заявки на рассрочку");

        InstallmentApplicationStatus status = application.getStatus();

        if ((status == InstallmentApplicationStatus.REJECTED
                || status == InstallmentApplicationStatus.RETURNED)
                && (application.getDecisionReason() == null
                || application.getDecisionReason().isBlank())) {

            throw new ValidationException(
                    "Для отклонённой или возвращённой заявки необходимо указать причину"
            );
        }

        Long reviewerId = null;

        if (application.getReviewer() != null) {
            reviewerId = application.getReviewer().getUserId();
            validateId(reviewerId, "проверяющего сотрудника");
        }

        String sql = """
        UPDATE installment_applications
        SET reviewer_id = ?,
            decision_date = ?,
            status = ?,
            decision_reason = ?
        WHERE application_id = ?
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (reviewerId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, reviewerId);
            }

            if (application.getDecisionDate() == null) {
                statement.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(application.getDecisionDate())
                );
            }

            statement.setString(3, status.name());
            statement.setString(4, application.getDecisionReason());
            statement.setLong(5, applicationId);

            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                throw new ValidationException(
                        "Заявка с идентификатором "
                                + applicationId + " не найдена"
                );
            }

        } catch (SQLException exception) {
            if ("23503".equals(exception.getSQLState())) {
                throw new ValidationException(
                        "Указанный проверяющий сотрудник не найден"
                );
            }

            throw new DataAccessException(
                    "Не удалось обновить заявку на рассрочку",
                    exception
            );
        }
    }
    /**
     * Проверяет корректность идентификатора.
     *
     * @param id идентификатор
     * @param entityName название сущности
     */
    private void validateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор " + entityName
                            + " должен быть положительным"
            );
        }
    }
    /**
     * Блокирует строку заявки на рассрочку
     * до завершения текущей транзакции.
     *
     * Используется для предотвращения параллельной
     * регистрации первоначальных взносов и платежей по графику.
     *
     * @param connection соединение текущей транзакции
     * @param applicationId идентификатор заявки
     */
    @Override
    public void lockById(Connection connection, Long applicationId) {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Соединение с базой данных не должно быть null"
            );
        }

        validateId(applicationId, "заявки на рассрочку");

        String sql = """
            SELECT application_id
            FROM installment_applications
            WHERE application_id = ?
            FOR UPDATE
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new ValidationException(
                            "Заявка на рассрочку с идентификатором "
                                    + applicationId + " не найдена"
                    );
                }
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось заблокировать заявку на рассрочку "
                            + applicationId,
                    exception
            );
        }
    }
}
