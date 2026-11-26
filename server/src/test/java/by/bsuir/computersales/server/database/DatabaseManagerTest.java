
package by.bsuir.computersales.server.database;

import by.bsuir.computersales.server.config.ServerConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void shouldConnectToPostgreSqlDatabase() throws Exception {
        // Используем отдельную тестовую конфигурацию.
        Path configPath = Path.of("../server-test.properties");

        assertTrue(
                Files.exists(configPath),
                "Файл server-test.properties не найден в корне проекта"
        );

        // Загружаем настройки тестового сервера.
        ServerConfig serverConfig = ServerConfig.load(configPath);

        // Создаём пул подключений.
        try (DatabaseManager databaseManager =
                     new DatabaseManager(serverConfig)) {

            // Получаем соединение с PostgreSQL.
            try (Connection connection =
                         databaseManager.getConnection()) {

                assertNotNull(connection);
                assertTrue(connection.isValid(5));

                // Проверяем название тестовой базы данных.
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(
                             "SELECT current_database()"
                     )) {

                    assertTrue(resultSet.next());

                    assertEquals(
                            "installment_shop_test",
                            resultSet.getString(1)
                    );
                }
            }
        }
    }
}
