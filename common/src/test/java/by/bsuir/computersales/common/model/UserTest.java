
package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUser() {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = new User(
                1L,
                "admin",
                null,
                true,
                createdAt
        );

        assertEquals(1L, user.getUserId());
        assertEquals("admin", user.getLogin());
        assertTrue(user.isActive());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void shouldIncludeUserDataInToString() {
        User user = new User(
                1L,
                "admin",
                null,
                true,
                LocalDateTime.of(2026, 11, 20, 10, 0)
        );

        String result = user.toString();

        assertTrue(result.contains("admin"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("active=true"));
    }

    @Test
    void shouldNotContainPasswordHashInToString() {
        User user = new User();

        String result = user.toString();

        assertFalse(result.toLowerCase().contains("password"));
        assertFalse(result.toLowerCase().contains("hash"));
    }
}

