
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
        // Проверяем наличие локального файла настроек.
        Path configPath = Path.of("../server.properties");

        assertTrue(
                Files.exists(configPath),
                "Файл server.properties не найден в корне проекта"
        );

        // Загружаем настройки сервера.
        ServerConfig serverConfig = ServerConfig.load(configPath);

        // Создаём пул подключений.
        try (DatabaseManager databaseManager =
                     new DatabaseManager(serverConfig)) {

            // Получаем соединение с PostgreSQL.
            try (Connection connection =
                         databaseManager.getConnection()) {

                assertNotNull(connection);
                assertTrue(connection.isValid(5));

                // Проверяем название подключённой базы данных.
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(
                             "SELECT current_database()"
                     )) {

                    assertTrue(resultSet.next());

                    assertEquals(
                            "installment_shop",
                            resultSet.getString(1)
                    );
                }
            }
        }
    }
}
