
package by.bsuir.computersales.client.server.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadConfiguration() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=8080\n"
                        + "server.thread-pool-size=20\n"
                        + "server.session-timeout-seconds=1800\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.password=secret\n"
                        + "database.pool-size=10\n"
        );

        ServerConfig config = ServerConfig.load(file);

        assertEquals("127.0.0.1", config.getServerHost());
        assertEquals(8080, config.getServerPort());
        assertEquals(20, config.getThreadPoolSize());
        assertEquals(1800, config.getSessionTimeoutSeconds());

        assertEquals(
                "jdbc:postgresql://localhost:5432/computer_sales",
                config.getDatabaseUrl()
        );
        assertEquals("postgres", config.getDatabaseUsername());
        assertEquals("secret", config.getDatabasePassword());
        assertEquals(10, config.getDatabasePoolSize());
    }

    @Test
    void shouldRejectMissingRequiredProperty() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=8080\n"
                        + "server.thread-pool-size=20\n"
                        + "server.session-timeout-seconds=1800\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.pool-size=10\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidServerPort() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=70000\n"
                        + "server.thread-pool-size=20\n"
                        + "server.session-timeout-seconds=1800\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.password=secret\n"
                        + "database.pool-size=10\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidDatabasePoolSize() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=8080\n"
                        + "server.thread-pool-size=20\n"
                        + "server.session-timeout-seconds=1800\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.password=secret\n"
                        + "database.pool-size=0\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidThreadPoolSize() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=8080\n"
                        + "server.thread-pool-size=0\n"
                        + "server.session-timeout-seconds=1800\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.password=secret\n"
                        + "database.pool-size=10\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidSessionTimeout() throws IOException {
        Path file = createConfigFile(
                "server.host=127.0.0.1\n"
                        + "server.port=8080\n"
                        + "server.thread-pool-size=20\n"
                        + "server.session-timeout-seconds=0\n"
                        + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                        + "database.username=postgres\n"
                        + "database.password=secret\n"
                        + "database.pool-size=10\n"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    private Path createConfigFile(String content) throws IOException {
        Path file = tempDir.resolve("server.properties");
        Files.writeString(file, content);
        return file;
    }
}
