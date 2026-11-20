
package by.bsuir.computersales.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldCreateRole() {
        Role role = new Role(
                1L,
                RoleType.ADMIN,
                "Администратор системы"
        );

        assertEquals(1L, role.getRoleId());
        assertEquals(RoleType.ADMIN, role.getRoleType());
        assertEquals("Администратор системы", role.getDescription());
    }

    @Test
    void shouldConsiderRolesWithSameIdEqual() {
        Role first = new Role(
                1L, RoleType.ADMIN, "Администратор"
        );

        Role second = new Role(
                1L, RoleType.SALES_MANAGER, "Менеджер"
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderRolesWithDifferentIdsNotEqual() {
        Role first = new Role(
                1L, RoleType.ADMIN, "Администратор"
        );

        Role second = new Role(
                2L, RoleType.ADMIN, "Администратор"
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotConsiderDifferentRolesWithoutIdEqual() {
        Role first = new Role(
                null, RoleType.ADMIN, "Администратор"
        );

        Role second = new Role(
                null, RoleType.ADMIN, "Администратор"
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldRejectNullRoleType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Role(1L, null, "Администратор")
        );

        Role role = new Role(
                1L, RoleType.ADMIN, "Администратор"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> role.setRoleType(null)
        );
    }

    @Test
    void shouldReturnDescriptionInToString() {
        Role role = new Role(
                1L,
                RoleType.SALES_MANAGER,
                "Менеджер по продажам"
        );

        assertEquals("Менеджер по продажам", role.toString());
    }
}
