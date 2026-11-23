
package by.bsuir.computersales.server.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadConfiguration() throws IOException {
        Path file = createConfigFile(validConfiguration());

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

        assertEquals(3, config.getMinInstallmentTerm());
        assertEquals(24, config.getMaxInstallmentTerm());
        assertEquals(
                new BigDecimal("10"),
                config.getMinDownPaymentPercent()
        );
        assertEquals(
                new BigDecimal("10000.00"),
                config.getMaxFinancedAmount()
        );
        assertEquals(2, config.getMaxActiveInstallments());
    }

    @Test
    void shouldRejectMissingRequiredProperty() throws IOException {
        String configuration = validConfiguration().replace(
                "database.password=secret\n",
                ""
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidServerPort() throws IOException {
        String configuration = validConfiguration().replace(
                "server.port=8080",
                "server.port=70000"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidDatabasePoolSize() throws IOException {
        String configuration = validConfiguration().replace(
                "database.pool-size=10",
                "database.pool-size=0"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidThreadPoolSize() throws IOException {
        String configuration = validConfiguration().replace(
                "server.thread-pool-size=20",
                "server.thread-pool-size=0"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidSessionTimeout() throws IOException {
        String configuration = validConfiguration().replace(
                "server.session-timeout-seconds=1800",
                "server.session-timeout-seconds=0"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidInstallmentTermRange() throws IOException {
        String configuration = validConfiguration().replace(
                "rules.term.max=24",
                "rules.term.max=2"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidDownPaymentPercent() throws IOException {
        String configuration = validConfiguration().replace(
                "rules.down_payment.min_percent=10",
                "rules.down_payment.min_percent=101"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidMaximumFinancedAmount() throws IOException {
        String configuration = validConfiguration().replace(
                "rules.financed.max=10000.00",
                "rules.financed.max=0"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    @Test
    void shouldRejectInvalidMaximumActiveInstallments() throws IOException {
        String configuration = validConfiguration().replace(
                "rules.active.max=2",
                "rules.active.max=0"
        );

        Path file = createConfigFile(configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> ServerConfig.load(file)
        );
    }

    private String validConfiguration() {
        return "server.host=127.0.0.1\n"
                + "server.port=8080\n"
                + "server.thread-pool-size=20\n"
                + "server.session-timeout-seconds=1800\n"
                + "database.url=jdbc:postgresql://localhost:5432/computer_sales\n"
                + "database.username=postgres\n"
                + "database.password=secret\n"
                + "database.pool-size=10\n"
                + "rules.term.min=3\n"
                + "rules.term.max=24\n"
                + "rules.down_payment.min_percent=10\n"
                + "rules.financed.max=10000.00\n"
                + "rules.active.max=2\n";
    }

    private Path createConfigFile(String content) throws IOException {
        Path file = tempDir.resolve("server.properties");
        Files.writeString(file, content);
        return file;
    }
}
