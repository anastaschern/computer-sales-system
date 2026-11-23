
package by.bsuir.computersales.server.database;

import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.exception.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Управляет пулом подключений к базе данных
 * и транзакциями приложения.
 */
public class DatabaseManager implements AutoCloseable {

    private final HikariDataSource dataSource;

    /**
     * Операция, выполняемая внутри транзакции.
     *
     * @param <T> тип результата
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {

        T execute(Connection connection) throws SQLException;
    }

    /**
     * Создаёт пул подключений на основании настроек сервера.
     *
     * @param serverConfig настройки сервера и базы данных
     */
    public DatabaseManager(ServerConfig serverConfig) {
        if (serverConfig == null) {
            throw new IllegalArgumentException(
                    "Настройки сервера не должны быть null"
            );
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(serverConfig.getDatabaseUrl());
        config.setUsername(serverConfig.getDatabaseUsername());
        config.setPassword(serverConfig.getDatabasePassword());

        config.setMaximumPoolSize(serverConfig.getDatabasePoolSize());
        config.setPoolName("ComputerSalesPool");
        config.setConnectionTimeout(30000);

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Возвращает соединение с базой данных.
     *
     * @return соединение JDBC
     * @throws SQLException если получить соединение не удалось
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Выполняет операцию в одной транзакции.
     *
     * Если операция завершается успешно, изменения фиксируются.
     * Если возникает ошибка, все изменения отменяются.
     *
     * @param callback операция, выполняемая в транзакции
     * @param <T> тип результата
     * @return результат операции
     */
    public <T> T executeInTransaction(
            TransactionCallback<T> callback) {

        if (callback == null) {
            throw new IllegalArgumentException(
                    "Операция транзакции не должна быть null"
            );
        }

        try (Connection connection = getConnection()) {

            connection.setAutoCommit(false);

            try {
                T result = callback.execute(connection);

                connection.commit();

                return result;

            } catch (SQLException | RuntimeException exception) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                if (exception instanceof SQLException) {
                    throw new DataAccessException(
                            "Ошибка выполнения транзакции",
                            exception
                    );
                }

                throw (RuntimeException) exception;
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось выполнить транзакцию с базой данных",
                    exception
            );
        }
    }

    /**
     * Закрывает пул подключений.
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
