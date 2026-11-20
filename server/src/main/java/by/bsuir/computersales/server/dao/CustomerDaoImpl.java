
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Customer;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к данным о покупателях.
 */
public class CustomerDaoImpl implements CustomerDao {

    /**
     * SQL-запрос для получения всех покупателей.
     */
    private static final String FIND_ALL_SQL = """
            SELECT customer_id,
                   full_name,
                   phone,
                   email,
                   document_reference
            FROM customers
            ORDER BY customer_id
            """;

    /**
     * SQL-запрос для поиска покупателя по идентификатору.
     */
    private static final String FIND_BY_ID_SQL = """
            SELECT customer_id,
                   full_name,
                   phone,
                   email,
                   document_reference
            FROM customers
            WHERE customer_id = ?
            """;

    private final DatabaseManager databaseManager;

    /**
     * Создаёт DAO покупателей.
     *
     * @param databaseManager менеджер подключений к базе данных
     */
    public CustomerDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                customers.add(mapRow(resultSet));
            }

            return customers;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить список покупателей",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Customer> findById(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор покупателя должен быть положительным"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось найти покупателя по идентификатору",
                    exception
            );
        }
    }

    /**
     * Преобразует строку результата SQL-запроса в объект Customer.
     *
     * @param resultSet результат SQL-запроса
     * @return объект покупателя
     * @throws SQLException при ошибке чтения данных
     */
    private Customer mapRow(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getLong("customer_id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("document_reference")
        );
    }
}
