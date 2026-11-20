
package by.bsuir.computersales.client.server.database;

import by.bsuir.computersales.client.server.config.ServerConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Управляет пулом подключений к базе данных.
 *
 * HikariCP позволяет повторно использовать открытые соединения,
 * что уменьшает затраты на подключение к PostgreSQL.
 */
public class DatabaseManager implements AutoCloseable {

    private final HikariDataSource dataSource;

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

        // Параметры подключения к PostgreSQL.
        config.setJdbcUrl(serverConfig.getDatabaseUrl());
        config.setUsername(serverConfig.getDatabaseUsername());
        config.setPassword(serverConfig.getDatabasePassword());

        // Максимальное количество соединений в пуле.
        config.setMaximumPoolSize(serverConfig.getDatabasePoolSize());

        // Имя пула для удобства диагностики.
        config.setPoolName("ComputerSalesPool");

        // Проверка соединения при получении из пула.
        config.setConnectionTimeout(30000);

        // Создание пула подключений.
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
     * Закрывает пул подключений.
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
