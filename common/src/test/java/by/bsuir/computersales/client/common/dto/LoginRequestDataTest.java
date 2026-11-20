
package by.bsuir.computersales.client.common.dto;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDataTest {

    @Test
    void shouldCreateLoginRequestData() {
        LoginRequestData data = new LoginRequestData(
                "admin",
                "password123"
        );

        assertEquals("admin", data.login());
        assertEquals("password123", data.password());
    }

    @Test
    void shouldImplementSerializable() {
        LoginRequestData data = new LoginRequestData(
                "admin",
                "password123"
        );

        assertInstanceOf(Serializable.class, data);
    }

    @Test
    void shouldRejectNullLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginRequestData(null, "password123")
        );
    }

    @Test
    void shouldRejectBlankLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginRequestData("   ", "password123")
        );
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginRequestData("admin", null)
        );
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginRequestData("admin", "   ")
        );
    }

    @Test
    void shouldHidePasswordInToString() {
        LoginRequestData data = new LoginRequestData(
                "admin",
                "SecretPassword123"
        );

        String result = data.toString();

        assertFalse(result.contains("SecretPassword123"));
        assertTrue(result.contains("password=***"));
        assertTrue(result.contains("admin"));
    }
    @Test
    void shouldAcceptShortLogin() {
        assertDoesNotThrow(
                () -> new LoginRequestData("ab", "password123")
        );
    }

    @Test
    void shouldAcceptShortPassword() {
        assertDoesNotThrow(
                () -> new LoginRequestData("user123", "123")
        );
    }
}
