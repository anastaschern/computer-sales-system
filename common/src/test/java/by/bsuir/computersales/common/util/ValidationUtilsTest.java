
package by.bsuir.computersales.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void shouldAcceptValidLogin() {
        assertDoesNotThrow(
                () -> ValidationUtils.validateLogin("admin_01")
        );
    }

    @Test
    void shouldRejectNullLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validateLogin(null)
        );
    }

    @Test
    void shouldRejectBlankLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validateLogin("   ")
        );
    }

    @Test
    void shouldRejectLoginShorterThanThreeCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validateLogin("ab")
        );
    }

    @Test
    void shouldRejectLoginLongerThanThirtyCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validateLogin(
                        "abcdefghijklmnopqrstuvwxyz12345"
                )
        );
    }

    @Test
    void shouldRejectLoginWithInvalidCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validateLogin("user name")
        );
    }

    @Test
    void shouldAcceptValidPassword() {
        assertDoesNotThrow(
                () -> ValidationUtils.validatePassword("password123")
        );
    }

    @Test
    void shouldAcceptPasswordWithMinimumLength() {
        assertDoesNotThrow(
                () -> ValidationUtils.validatePassword("12345678")
        );
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword(null)
        );
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword("        ")
        );
    }

    @Test
    void shouldRejectPasswordShorterThanEightCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword("1234567")
        );
    }
    @Test
    void shouldAcceptPasswordWithMaximumLength() {
        String password = "a".repeat(128);

        assertDoesNotThrow(
                () -> ValidationUtils.validatePassword(password)
        );
    }

    @Test
    void shouldRejectPasswordLongerThan128Characters() {
        String password = "a".repeat(129);

        assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword(password)
        );
    }
}

