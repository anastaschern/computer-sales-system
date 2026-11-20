
package by.bsuir.computersales.client.common.model;

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
    @Test
    void shouldConsiderUsersWithSameIdEqual() {
        User first = new User(
                1L, "admin", null, true, null
        );

        User second = new User(
                1L, "manager", null, false, null
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderUsersWithDifferentIdsNotEqual() {
        User first = new User(
                1L, "admin", null, true, null
        );

        User second = new User(
                2L, "admin", null, true, null
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotConsiderDifferentUsersWithoutIdEqual() {
        User first = new User(
                null, "admin", null, true, null
        );

        User second = new User(
                null, "admin", null, true, null
        );

        assertNotEquals(first, second);
    }
}

