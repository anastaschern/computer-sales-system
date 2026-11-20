
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.Product;
import by.bsuir.computersales.common.model.ProductType;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.DataAccessException;
import by.bsuir.computersales.server.factory.ProductFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к данным о товарах.
 */
public class ProductDaoImpl implements ProductDao {

    /**
     * SQL-запрос для получения всех товаров вместе с категориями.
     */
    private static final String FIND_ALL_SQL = """
            SELECT p.product_id,
                   p.manufacturer,
                   p.model,
                   p.product_name,
                   p.specification,
                   p.price,
                   p.stock_quantity,
                   p.is_active,
                   c.category_id,
                   c.category_name,
                   c.description AS category_description,
                   c.product_type
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            ORDER BY p.product_id
            """;

    /**
     * SQL-запрос для поиска товара по идентификатору.
     */
    private static final String FIND_BY_ID_SQL = """
            SELECT p.product_id,
                   p.manufacturer,
                   p.model,
                   p.product_name,
                   p.specification,
                   p.price,
                   p.stock_quantity,
                   p.is_active,
                   c.category_id,
                   c.category_name,
                   c.description AS category_description,
                   c.product_type
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            WHERE p.product_id = ?
            """;

    /**
     * SQL-запрос для получения активных товаров.
     */
    private static final String FIND_ALL_ACTIVE_SQL = """
            SELECT p.product_id,
                   p.manufacturer,
                   p.model,
                   p.product_name,
                   p.specification,
                   p.price,
                   p.stock_quantity,
                   p.is_active,
                   c.category_id,
                   c.category_name,
                   c.description AS category_description,
                   c.product_type
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            WHERE p.is_active = TRUE
            ORDER BY p.product_id
            """;

    private final DatabaseManager databaseManager;
    private final ProductFactory productFactory;

    /**
     * Создаёт DAO товаров.
     *
     * @param databaseManager менеджер подключений к базе данных
     */
    public ProductDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );

        this.productFactory = new ProductFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAll() {
        return executeListQuery(FIND_ALL_SQL, "получить список товаров");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findById(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор товара должен быть положительным"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось найти товар по идентификатору",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAllActive() {
        return executeListQuery(
                FIND_ALL_ACTIVE_SQL,
                "получить список активных товаров"
        );
    }

    /**
     * Выполняет SQL-запрос, возвращающий список товаров.
     *
     * @param sql SQL-запрос
     * @param operation описание операции
     * @return список товаров
     */
    private List<Product> executeListQuery(String sql, String operation) {
        List<Product> products = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRow(resultSet));
            }

            return products;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось " + operation,
                    exception
            );
        }
    }

    /**
     * Преобразует строку результата SQL-запроса в объект конкретного типа товара.
     *
     * @param resultSet результат SQL-запроса
     * @return объект товара
     * @throws SQLException при ошибке чтения данных
     */
    private Product mapRow(ResultSet resultSet) throws SQLException {
        ProductType productType = ProductType.valueOf(
                resultSet.getString("product_type")
        );

        Category category = new Category(
                resultSet.getLong("category_id"),
                resultSet.getString("category_name"),
                resultSet.getString("category_description"),
                productType
        );

        return productFactory.createProduct(
                resultSet.getLong("product_id"),
                category,
                resultSet.getString("manufacturer"),
                resultSet.getString("model"),
                resultSet.getString("product_name"),
                resultSet.getString("specification"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getBoolean("is_active")
        );
    }
}
