
package by.bsuir.computersales.server.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Выполняет безопасное хеширование паролей
 * и проверку введённых паролей.
 *
 * Используется алгоритм PBKDF2WithHmacSHA256.
 * Для каждого пароля генерируется отдельная случайная соль.
 */
public class PasswordHasher {

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final int ITERATIONS = 210_000;

    private static final int KEY_LENGTH = 256;

    private static final int SALT_LENGTH = 16;

    private static final int MIN_ITERATIONS = 10_000;

    private static final int MAX_ITERATIONS = 1_000_000;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    /**
     * Создаёт строковое представление хеша пароля.
     *
     * Формат:
     * алгоритм$число_итераций$соль$хеш
     *
     * @param password пароль пользователя
     * @return строка с параметрами хеширования
     */
    public String hashPassword(char[] password) {
        validatePassword(password);

        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(
                password,
                salt,
                ITERATIONS
        );

        return "pbkdf2_sha256"
                + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Проверяет соответствие пароля сохранённому хешу.
     *
     * @param password введённый пароль
     * @param storedHash сохранённый хеш
     * @return true, если пароль совпадает
     */
    public boolean verifyPassword(
            char[] password,
            String storedHash
    ) {
        if (password == null
                || password.length == 0
                || storedHash == null
                || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);

        if (parts.length != 4
                || !"pbkdf2_sha256".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);

            if (iterations < MIN_ITERATIONS
                    || iterations > MAX_ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length != SALT_LENGTH
                    || expectedHash.length != KEY_LENGTH / 8) {
                return false;
            }

            byte[] actualHash = deriveKey(
                    password,
                    salt,
                    iterations
            );

            return MessageDigest.isEqual(
                    expectedHash,
                    actualHash
            );

        } catch (NumberFormatException
                 | IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Получает криптографический ключ из пароля и соли.
     */
    private byte[] deriveKey(
            char[] password,
            byte[] salt,
            int iterations
    ) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(keySpec).getEncoded();

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Не удалось выполнить хеширование пароля",
                    exception
            );

        } finally {
            keySpec.clearPassword();
        }
    }

    /**
     * Проверяет корректность входного пароля.
     */
    private void validatePassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "Пароль не должен быть пустым"
            );
        }
    }
}
