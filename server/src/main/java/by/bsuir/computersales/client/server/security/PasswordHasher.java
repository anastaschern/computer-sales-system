
package by.bsuir.computersales.client.server.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Выполняет хеширование и проверку пользовательских паролей.
 *
 * Использует алгоритм PBKDF2-HMAC-SHA256.
 * Для каждого пароля генерируется отдельная случайная соль.
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final String HASH_PREFIX = "pbkdf2-sha256";

    private static final int ITERATIONS = 600_000;

    private static final int MIN_ITERATIONS = 100_000;

    private static final int MAX_ITERATIONS = 2_000_000;

    private static final int SALT_LENGTH_BYTES = 16;

    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Создаёт хеш пароля с уникальной случайной солью.
     *
     * Формат результата:
     * алгоритм$число_итераций$соль$хеш
     *
     * @param password исходный пароль
     * @return строка с параметрами хеширования и результатом
     */
    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Пароль не должен быть пустым"
            );
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(
                password.toCharArray(),
                salt,
                ITERATIONS
        );

        return HASH_PREFIX + "$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Проверяет соответствие пароля сохранённому хешу.
     *
     * @param password проверяемый пароль
     * @param storedHash сохранённый хеш
     * @return true, если пароль правильный
     */
    public boolean verifyPassword(String password, String storedHash) {
        if (password == null || password.isEmpty()
                || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);

        if (parts.length != 4 || !HASH_PREFIX.equals(parts[0])) {
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

            if (salt.length != SALT_LENGTH_BYTES
                    || expectedHash.length != KEY_LENGTH_BITS / 8) {
                return false;
            }

            byte[] actualHash = deriveKey(
                    password.toCharArray(),
                    salt,
                    iterations
            );

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Вычисляет ключ на основе пароля и соли.
     *
     * @param password пароль в виде массива символов
     * @param salt случайная соль
     * @param iterations количество итераций
     * @return результат PBKDF2
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
                KEY_LENGTH_BITS
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

            java.util.Arrays.fill(password, '\0');
        }
    }
}
