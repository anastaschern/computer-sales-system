
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.LoginResult;
import by.bsuir.computersales.common.model.User;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class LoginResultTest {

    @Test
    void shouldCreateLoginResult() {
        User user = new User();

        LoginResult result = new LoginResult(
                "session-token-123",
                user
        );

        assertEquals("session-token-123", result.sessionToken());
        assertSame(user, result.user());
    }

    @Test
    void shouldImplementSerializable() {
        LoginResult result = new LoginResult(
                "session-token-123",
                new User()
        );

        assertInstanceOf(Serializable.class, result);
    }

    @Test
    void shouldRejectNullSessionToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginResult(null, new User())
        );
    }

    @Test
    void shouldRejectBlankSessionToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginResult("   ", new User())
        );
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginResult("session-token-123", null)
        );
    }
    @Test
    void shouldHideSessionTokenInToString() {
        LoginResult result = new LoginResult(
                "SecretSessionToken123",
                new User()
        );

        String text = result.toString();

        assertFalse(text.contains("SecretSessionToken123"));
        assertTrue(text.contains("sessionToken=***"));
    }
}
