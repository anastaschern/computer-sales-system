
package by.bsuir.computersales.server.security;

import by.bsuir.computersales.server.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(3600);
    }

    @Test
    void createSessionShouldReturnUniqueToken() {
        String firstToken = sessionManager.createSession(1L);
        String secondToken = sessionManager.createSession(1L);

        assertNotNull(firstToken);
        assertNotNull(secondToken);
        assertNotEquals(firstToken, secondToken);
        assertEquals(2, sessionManager.getActiveSessionCount());
    }

    @Test
    void getUserIdShouldReturnCorrectUserId() {
        String token = sessionManager.createSession(15L);

        Long userId = sessionManager.getUserId(token);

        assertEquals(15L, userId);
    }

    @Test
    void createSessionShouldRejectInvalidUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sessionManager.createSession(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> sessionManager.createSession(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> sessionManager.createSession(-1L)
        );
    }

    @Test
    void constructorShouldRejectInvalidTimeout() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SessionManager(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SessionManager(-1)
        );
    }

    @Test
    void getUserIdShouldRejectInvalidToken() {
        assertThrows(
                AuthenticationException.class,
                () -> sessionManager.getUserId(null)
        );

        assertThrows(
                AuthenticationException.class,
                () -> sessionManager.getUserId("")
        );

        assertThrows(
                AuthenticationException.class,
                () -> sessionManager.getUserId("   ")
        );

        assertThrows(
                AuthenticationException.class,
                () -> sessionManager.getUserId("unknown-token")
        );
    }

    @Test
    void invalidateSessionShouldRemoveSession() {
        String token = sessionManager.createSession(10L);

        sessionManager.invalidateSession(token);

        assertEquals(0, sessionManager.getActiveSessionCount());

        assertThrows(
                AuthenticationException.class,
                () -> sessionManager.getUserId(token)
        );
    }

    @Test
    void invalidateAllShouldRemoveAllSessions() {
        sessionManager.createSession(1L);
        sessionManager.createSession(2L);
        sessionManager.createSession(3L);

        sessionManager.invalidateAll();

        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    void getUserIdShouldRejectExpiredSession() throws InterruptedException {
        SessionManager shortSessionManager = new SessionManager(1);

        String token = shortSessionManager.createSession(25L);

        Thread.sleep(1200);

        assertThrows(
                AuthenticationException.class,
                () -> shortSessionManager.getUserId(token)
        );

        assertEquals(0, shortSessionManager.getActiveSessionCount());
    }

    @Test
    void invalidateSessionShouldHandleNullToken() {
        sessionManager.createSession(1L);

        assertDoesNotThrow(
                () -> sessionManager.invalidateSession(null)
        );

        assertEquals(1, sessionManager.getActiveSessionCount());
    }
}
