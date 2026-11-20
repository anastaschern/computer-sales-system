
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.dto.SaleItemData;
import by.bsuir.computersales.common.model.*;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import by.bsuir.computersales.server.exception.DataAccessException;
import by.bsuir.computersales.server.exception.ValidationException;
import by.bsuir.computersales.server.factory.ProductFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * JDBC-реализация доступа к данным о продажах.
 */
public class SaleDaoImpl implements SaleDao {

    private static final String SELECT_SALES_SQL = """
            SELECT s.sale_id,
                   s.sale_date,
                   s.status,
                   s.payment_type,

                   c.customer_id,
                   c.full_name,
                   c.phone,
                   c.email,
                   c.document_reference,

                   u.user_id,
                   u.login,
                   u.is_active,
                   u.created_at,

                   r.role_id,
                   r.role_name,
                   r.description AS role_description,

                   si.line_number,
                   si.quantity,
                   si.unit_price,

                   p.product_id,
                   p.manufacturer,
                   p.model,
                   p.product_name,
                   p.specification,
                   p.price,
                   p.stock_quantity,
                   p.is_active AS product_is_active,

                   cat.category_id,
                   cat.category_name,
                   cat.description AS category_description,
                   cat.product_type

            FROM sales s
            JOIN customers c ON s.customer_id = c.customer_id
            JOIN users u ON s.user_id = u.user_id
            JOIN roles r ON u.role_id = r.role_id
            LEFT JOIN sale_items si ON s.sale_id = si.sale_id
            LEFT JOIN products p ON si.product_id = p.product_id
            LEFT JOIN categories cat ON p.category_id = cat.category_id
            """;

    private static final String FIND_ALL_SQL =
            SELECT_SALES_SQL + " ORDER BY s.sale_id, si.line_number";

    private static final String FIND_BY_ID_SQL =
            SELECT_SALES_SQL
                    + " WHERE s.sale_id = ? ORDER BY si.line_number";

    private static final String FIND_BY_CUSTOMER_ID_SQL =
            SELECT_SALES_SQL
                    + " WHERE s.customer_id = ? "
                    + "ORDER BY s.sale_date DESC, s.sale_id, si.line_number";

    private static final String FIND_BY_USER_ID_SQL =
            SELECT_SALES_SQL
                    + " WHERE s.user_id = ? "
                    + "ORDER BY s.sale_date DESC, s.sale_id, si.line_number";

    private static final String CHECK_CUSTOMER_SQL = """
            SELECT 1
            FROM customers
            WHERE customer_id = ?
            """;

    private static final String CHECK_USER_SQL = """
            SELECT 1
            FROM users
            WHERE user_id = ?
              AND is_active = TRUE
            """;

    private static final String INSERT_SALE_SQL = """
            INSERT INTO sales (customer_id, user_id, payment_type)
            VALUES (?, ?, ?)
            RETURNING sale_id
            """;

    private static final String UPDATE_PRODUCT_STOCK_SQL = """
            UPDATE products
            SET stock_quantity = stock_quantity - ?
            WHERE product_id = ?
              AND is_active = TRUE
              AND stock_quantity >= ?
            RETURNING price
            """;

    private static final String INSERT_SALE_ITEM_SQL = """
            INSERT INTO sale_items (
                sale_id,
                line_number,
                product_id,
                quantity,
                unit_price
            )
            VALUES (?, ?, ?, ?, ?)
            """;

    private final DatabaseManager databaseManager;
    private final ProductFactory productFactory;

    public SaleDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );

        this.productFactory = new ProductFactory();
    }

    @Override
    public List<Sale> findAll() {
        return executeListQuery(
                FIND_ALL_SQL,
                null,
                "получить список продаж"
        );
    }

    @Override
    public Optional<Sale> findById(Long saleId) {
        validateId(saleId, "продажи");

        List<Sale> sales = executeListQuery(
                FIND_BY_ID_SQL,
                saleId,
                "найти продажу по идентификатору"
        );

        return sales.stream().findFirst();
    }

    @Override
    public List<Sale> findByCustomerId(Long customerId) {
        validateId(customerId, "покупателя");

        return executeListQuery(
                FIND_BY_CUSTOMER_ID_SQL,
                customerId,
                "получить продажи покупателя"
        );
    }

    @Override
    public List<Sale> findByUserId(Long userId) {
        validateId(userId, "сотрудника");

        return executeListQuery(
                FIND_BY_USER_ID_SQL,
                userId,
                "получить продажи сотрудника"
        );
    }

    /**
     * Создаёт продажу и уменьшает остатки товаров
     * в рамках одной транзакции.
     */
    @Override
    public Long createSale(Long userId, CreateSaleData saleData) {
        validateId(userId, "сотрудника");

        if (saleData == null) {
            throw new ValidationException(
                    "Данные продажи не должны быть null"
            );
        }

        List<SaleItemData> items = saleData.items();

        if (items == null || items.isEmpty()) {
            throw new ValidationException(
                    "Продажа должна содержать хотя бы один товар"
            );
        }

        // Не допускаем повторения одного товара в разных позициях.
        Set<Long> productIds = new HashSet<>();

        for (SaleItemData item : items) {
            if (item == null) {
                throw new ValidationException(
                        "Позиция продажи не должна быть null"
                );
            }

            if (!productIds.add(item.productId())) {
                throw new ValidationException(
                        "Один товар не должен повторяться "
                                + "в нескольких позициях продажи"
                );
            }
        }

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Проверяем существование покупателя.
                checkCustomerExists(connection, saleData.customerId());

                // Проверяем существование и активность сотрудника.
                checkActiveUserExists(connection, userId);

                // Создаём основную запись продажи.
                Long saleId = insertSale(connection, userId, saleData);

                // Добавляем товарные позиции и уменьшаем остатки.
                int lineNumber = 1;

                for (SaleItemData item : items) {
                    insertSaleItem(
                            connection,
                            saleId,
                            lineNumber,
                            item
                    );

                    lineNumber++;
                }

                connection.commit();

                return saleId;

            } catch (SQLException exception) {
                rollback(connection, exception);

                throw new DataAccessException(
                        "Не удалось оформить продажу",
                        exception
                );

            } catch (RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить соединение "
                            + "для оформления продажи",
                    exception
            );
        }
    }

    /**
     * Проверяет существование покупателя.
     */
    private void checkCustomerExists(
            Connection connection,
            Long customerId) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(CHECK_CUSTOMER_SQL)) {

            statement.setLong(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new BusinessRuleException(
                            "Покупатель с идентификатором "
                                    + customerId + " не найден"
                    );
                }
            }
        }
    }

    /**
     * Проверяет существование и активность сотрудника.
     */
    private void checkActiveUserExists(
            Connection connection,
            Long userId) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(CHECK_USER_SQL)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new BusinessRuleException(
                            "Сотрудник не найден или неактивен"
                    );
                }
            }
        }
    }

    /**
     * Добавляет основную запись продажи.
     *
     * @return идентификатор созданной продажи
     */
    private Long insertSale(
            Connection connection,
            Long userId,
            CreateSaleData saleData) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_SALE_SQL)) {

            statement.setLong(1, saleData.customerId());
            statement.setLong(2, userId);
            statement.setString(
                    3,
                    saleData.paymentType().name()
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException(
                            "База данных не вернула идентификатор продажи"
                    );
                }

                return resultSet.getLong("sale_id");
            }
        }
    }

    /**
     * Добавляет товарную позицию продажи.
     *
     * Перед добавлением позиции остаток товара уменьшается.
     * Цена фиксируется на момент оформления продажи.
     */
    private void insertSaleItem(
            Connection connection,
            Long saleId,
            int lineNumber,
            SaleItemData item) throws SQLException {

        java.math.BigDecimal unitPrice;

        // Уменьшаем остаток только при наличии нужного количества.
        // Условие UPDATE защищает от продажи одного остатка
        // одновременно несколькими сотрудниками.
        try (PreparedStatement statement =
                     connection.prepareStatement(UPDATE_PRODUCT_STOCK_SQL)) {

            statement.setInt(1, item.quantity());
            statement.setLong(2, item.productId());
            statement.setInt(3, item.quantity());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new BusinessRuleException(
                            "Товар с идентификатором "
                                    + item.productId()
                                    + " отсутствует, неактивен "
                                    + "или его остатка недостаточно"
                    );
                }

                unitPrice = resultSet.getBigDecimal("price");
            }
        }

        // Сохраняем цену из базы данных, а не от клиента.
        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_SALE_ITEM_SQL)) {

            statement.setLong(1, saleId);
            statement.setInt(2, lineNumber);
            statement.setLong(3, item.productId());
            statement.setInt(4, item.quantity());
            statement.setBigDecimal(5, unitPrice);

            statement.executeUpdate();
        }
    }

    /**
     * Отменяет транзакцию.
     * Если откат также завершился ошибкой, она добавляется
     * к исходному исключению.
     */
    private void rollback(
            Connection connection,
            Throwable originalException) {

        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private List<Sale> executeListQuery(
            String sql,
            Long parameter,
            String operation) {

        Map<Long, Sale> sales = new LinkedHashMap<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setLong(1, parameter);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Long saleId = resultSet.getLong("sale_id");

                    Sale sale = sales.computeIfAbsent(
                            saleId,
                            id -> mapSale(resultSet)
                    );

                    SaleItem item = mapSaleItem(resultSet);

                    if (item != null) {
                        sale.getItems().add(item);
                    }
                }
            }

            return new ArrayList<>(sales.values());

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось " + operation,
                    exception
            );
        }
    }

    private Sale mapSale(ResultSet resultSet) {
        try {
            Customer customer = new Customer(
                    resultSet.getLong("customer_id"),
                    resultSet.getString("full_name"),
                    resultSet.getString("phone"),
                    resultSet.getString("email"),
                    resultSet.getString("document_reference")
            );

            Role role = new Role(
                    resultSet.getLong("role_id"),
                    RoleType.valueOf(resultSet.getString("role_name")),
                    resultSet.getString("role_description")
            );

            User user = new User(
                    resultSet.getLong("user_id"),
                    resultSet.getString("login"),
                    role,
                    resultSet.getBoolean("is_active"),
                    resultSet.getTimestamp("created_at").toLocalDateTime()
            );

            return new Sale(
                    resultSet.getLong("sale_id"),
                    customer,
                    user,
                    resultSet.getTimestamp("sale_date").toLocalDateTime(),
                    SaleStatus.valueOf(resultSet.getString("status")),
                    PaymentType.valueOf(resultSet.getString("payment_type")),
                    new ArrayList<>()
            );

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось преобразовать данные продажи",
                    exception
            );
        }
    }

    private SaleItem mapSaleItem(ResultSet resultSet)
            throws SQLException {

        long productId = resultSet.getLong("product_id");

        if (resultSet.wasNull()) {
            return null;
        }

        ProductType productType = ProductType.valueOf(
                resultSet.getString("product_type")
        );

        Category category = new Category(
                resultSet.getLong("category_id"),
                resultSet.getString("category_name"),
                resultSet.getString("category_description"),
                productType
        );

        Product product = productFactory.createProduct(
                productId,
                category,
                resultSet.getString("manufacturer"),
                resultSet.getString("model"),
                resultSet.getString("product_name"),
                resultSet.getString("specification"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getBoolean("product_is_active")
        );

        return new SaleItem(
                resultSet.getInt("line_number"),
                product,
                resultSet.getInt("quantity"),
                resultSet.getBigDecimal("unit_price")
        );
    }

    private void validateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор " + entityName
                            + " должен быть положительным"
            );
        }
    }
}
