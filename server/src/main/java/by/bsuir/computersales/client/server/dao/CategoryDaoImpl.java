
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.Category;
import by.bsuir.computersales.client.common.model.ProductType;
import by.bsuir.computersales.client.server.database.DatabaseManager;
import by.bsuir.computersales.client.server.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к данным о категориях товаров.
 */
public class CategoryDaoImpl implements CategoryDao {

    /**
     * SQL-запрос для получения всех категорий.
     */
    private static final String FIND_ALL_SQL = """
            SELECT category_id,
                   category_name,
                   description,
                   product_type
            FROM categories
            ORDER BY category_id
            """;

    /**
     * SQL-запрос для поиска категории по идентификатору.
     */
    private static final String FIND_BY_ID_SQL = """
            SELECT category_id,
                   category_name,
                   description,
                   product_type
            FROM categories
            WHERE category_id = ?
            """;

    private final DatabaseManager databaseManager;

    /**
     * Создаёт DAO категорий.
     *
     * @param databaseManager менеджер подключений к базе данных
     */
    public CategoryDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }

            return categories;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить список категорий",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> findById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор категории должен быть положительным"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось найти категорию по идентификатору",
                    exception
            );
        }
    }

    /**
     * Преобразует строку результата SQL-запроса в объект Category.
     *
     * @param resultSet результат SQL-запроса
     * @return объект категории
     * @throws SQLException при ошибке чтения данных
     */
    private Category mapRow(ResultSet resultSet) throws SQLException {
        return new Category(
                resultSet.getLong("category_id"),
                resultSet.getString("category_name"),
                resultSet.getString("description"),
                ProductType.valueOf(resultSet.getString("product_type"))
        );
    }
}
