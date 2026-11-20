
package by.bsuir.computersales.server.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new PasswordHasher();
    }

    @Test
    void hashPasswordShouldReturnNonEmptyHash() {
        String hash = passwordHasher.hashPassword("MyPassword123");

        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertTrue(hash.startsWith("pbkdf2-sha256$"));
    }

    @Test
    void hashPasswordShouldGenerateDifferentHashesForSamePassword() {
        String firstHash = passwordHasher.hashPassword("MyPassword123");
        String secondHash = passwordHasher.hashPassword("MyPassword123");

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void verifyPasswordShouldAcceptCorrectPassword() {
        String password = "MyPassword123";
        String hash = passwordHasher.hashPassword(password);

        assertTrue(passwordHasher.verifyPassword(password, hash));
    }

    @Test
    void verifyPasswordShouldRejectIncorrectPassword() {
        String hash = passwordHasher.hashPassword("MyPassword123");

        assertFalse(
                passwordHasher.verifyPassword("WrongPassword123", hash)
        );
    }

    @Test
    void hashPasswordShouldRejectNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHasher.hashPassword(null)
        );
    }

    @Test
    void hashPasswordShouldRejectEmptyPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHasher.hashPassword("")
        );
    }

    @Test
    void verifyPasswordShouldRejectNullValues() {
        assertFalse(passwordHasher.verifyPassword(null, "some-hash"));
        assertFalse(passwordHasher.verifyPassword("password", null));
    }

    @Test
    void verifyPasswordShouldRejectMalformedHash() {
        assertFalse(
                passwordHasher.verifyPassword(
                        "password",
                        "invalid-hash"
                )
        );
    }

    @Test
    void verifyPasswordShouldRejectUnsupportedAlgorithm() {
        assertFalse(
                passwordHasher.verifyPassword(
                        "password",
                        "unknown-algorithm$600000$abc$def"
                )
        );
    }

    @Test
    void verifyPasswordShouldRejectInvalidIterationCount() {
        assertFalse(
                passwordHasher.verifyPassword(
                        "password",
                        "pbkdf2-sha256$1$YWJj$YWJj"
                )
        );
    }
}
