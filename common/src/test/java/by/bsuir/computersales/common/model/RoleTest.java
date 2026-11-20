package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldCreateRole() {
        Role role = new Role(
                1L,
                "ADMIN",
                "Администратор системы"
        );

        assertEquals(1L, role.getRoleId());
        assertEquals("ADMIN", role.getRoleName());
        assertEquals("Администратор системы", role.getDescription());
    }

    @Test
    void shouldConsiderRolesWithSameIdEqual() {
        Role first = new Role(1L, "ADMIN", "Описание 1");
        Role second = new Role(1L, "USER", "Описание 2");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderRolesWithDifferentIdsNotEqual() {
        Role first = new Role(1L, "ADMIN", "Описание");
        Role second = new Role(2L, "ADMIN", "Описание");

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotConsiderDifferentRolesWithoutIdEqual() {
        Role first = new Role(null, "ADMIN", "Описание");
        Role second = new Role(null, "ADMIN", "Описание");

        assertNotEquals(first, second);
    }
}