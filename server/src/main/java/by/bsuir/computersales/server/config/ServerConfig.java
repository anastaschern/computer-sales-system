
package by.bsuir.computersales.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Содержит настройки серверного приложения.
 *
 * Настройки загружаются из внешнего файла properties,
 * поэтому их можно изменять без редактирования исходного кода.
 */
public class ServerConfig {

    private final String serverHost;
    private final int serverPort;
    private final int threadPoolSize;
    private final int sessionTimeoutSeconds;

    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;
    private final int databasePoolSize;

    // Параметры правил рассмотрения заявок на рассрочку.
    private final int minInstallmentTerm;
    private final int maxInstallmentTerm;
    private final BigDecimal minDownPaymentPercent;
    private final BigDecimal maxFinancedAmount;
    private final int maxActiveInstallments;

    private ServerConfig(Properties properties) {
        this.serverHost = getRequiredProperty(
                properties, "server.host"
        );

        this.serverPort = getIntProperty(
                properties, "server.port"
        );

        this.threadPoolSize = getIntProperty(
                properties, "server.thread-pool-size"
        );

        this.sessionTimeoutSeconds = getIntProperty(
                properties, "server.session-timeout-seconds"
        );

        this.databaseUrl = getRequiredProperty(
                properties, "database.url"
        );

        this.databaseUsername = getRequiredProperty(
                properties, "database.username"
        );

        this.databasePassword = getRequiredProperty(
                properties, "database.password"
        );

        this.databasePoolSize = getIntProperty(
                properties, "database.pool-size"
        );

        this.minInstallmentTerm = getIntProperty(
                properties, "rules.term.min"
        );

        this.maxInstallmentTerm = getIntProperty(
                properties, "rules.term.max"
        );

        this.minDownPaymentPercent = getBigDecimalProperty(
                properties, "rules.down_payment.min_percent"
        );

        this.maxFinancedAmount = getBigDecimalProperty(
                properties, "rules.financed.max"
        );

        this.maxActiveInstallments = getIntProperty(
                properties, "rules.active.max"
        );

        validate();
    }

    /**
     * Загружает настройки из указанного файла.
     *
     * @param path путь к файлу настроек
     * @return объект конфигурации сервера
     */
    public static ServerConfig load(Path path) {
        if (path == null) {
            throw new IllegalArgumentException(
                    "Путь к файлу настроек не может быть null"
            );
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не удалось загрузить настройки сервера",
                    exception
            );
        }

        return new ServerConfig(properties);
    }

    /**
     * Получает обязательное строковое свойство.
     */
    private static String getRequiredProperty(
            Properties properties,
            String key
    ) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Не задан обязательный параметр: " + key
            );
        }

        return value.trim();
    }

    /**
     * Получает целочисленное свойство.
     */
    private static int getIntProperty(
            Properties properties,
            String key
    ) {
        String value = getRequiredProperty(properties, key);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Параметр " + key + " должен быть целым числом",
                    exception
            );
        }
    }

    /**
     * Получает свойство типа BigDecimal.
     */
    private static BigDecimal getBigDecimalProperty(
            Properties properties,
            String key
    ) {
        String value = getRequiredProperty(properties, key);

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Параметр " + key + " должен быть числом",
                    exception
            );
        }
    }

    /**
     * Проверяет допустимость настроек.
     */
    private void validate() {
        if (serverPort < 1 || serverPort > 65535) {
            throw new IllegalArgumentException(
                    "Порт сервера должен находиться в диапазоне от 1 до 65535"
            );
        }

        if (threadPoolSize < 1) {
            throw new IllegalArgumentException(
                    "Размер пула потоков должен быть положительным"
            );
        }

        if (sessionTimeoutSeconds < 1) {
            throw new IllegalArgumentException(
                    "Таймаут сессии должен быть положительным"
            );
        }

        if (databasePoolSize < 1) {
            throw new IllegalArgumentException(
                    "Размер пула соединений с базой данных должен быть положительным"
            );
        }

        if (minInstallmentTerm < 1
                || maxInstallmentTerm < minInstallmentTerm) {
            throw new IllegalArgumentException(
                    "Некорректные границы срока рассрочки"
            );
        }

        if (minDownPaymentPercent.compareTo(BigDecimal.ZERO) < 0
                || minDownPaymentPercent.compareTo(
                new BigDecimal("100")
        ) > 0) {
            throw new IllegalArgumentException(
                    "Минимальный первоначальный взнос должен быть от 0 до 100 процентов"
            );
        }

        if (maxFinancedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Максимальная сумма рассрочки должна быть положительной"
            );
        }

        if (maxActiveInstallments < 1) {
            throw new IllegalArgumentException(
                    "Максимальное количество действующих рассрочек должно быть положительным"
            );
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public int getDatabasePoolSize() {
        return databasePoolSize;
    }

    public int getMinInstallmentTerm() {
        return minInstallmentTerm;
    }

    public int getMaxInstallmentTerm() {
        return maxInstallmentTerm;
    }

    public BigDecimal getMinDownPaymentPercent() {
        return minDownPaymentPercent;
    }

    public BigDecimal getMaxFinancedAmount() {
        return maxFinancedAmount;
    }

    public int getMaxActiveInstallments() {
        return maxActiveInstallments;
    }

    /**
     * Не раскрывает пароль базы данных.
     */
    @Override
    public String toString() {
        return "ServerConfig["
                + "serverHost='" + serverHost + '\''
                + ", serverPort=" + serverPort
                + ", threadPoolSize=" + threadPoolSize
                + ", sessionTimeoutSeconds=" + sessionTimeoutSeconds
                + ", databaseUrl='" + databaseUrl + '\''
                + ", databaseUsername='" + databaseUsername + '\''
                + ", databasePassword=***"
                + ", databasePoolSize=" + databasePoolSize
                + ", minInstallmentTerm=" + minInstallmentTerm
                + ", maxInstallmentTerm=" + maxInstallmentTerm
                + ", minDownPaymentPercent=" + minDownPaymentPercent
                + ", maxFinancedAmount=" + maxFinancedAmount
                + ", maxActiveInstallments=" + maxActiveInstallments
                + ']';
    }

}
