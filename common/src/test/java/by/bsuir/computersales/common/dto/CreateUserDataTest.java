
package by.bsuir.computersales.common.dto;

import by.bsuir.computersales.common.CreateUserData;
import by.bsuir.computersales.common.model.RoleType;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserDataTest {

    @Test
    void shouldCreateUserData() {
        CreateUserData data = new CreateUserData(
                "manager",
                "password123",
                RoleType.SALES_MANAGER
        );

        assertEquals("manager", data.login());
        assertEquals("password123", data.password());
        assertEquals(RoleType.SALES_MANAGER, data.role());
    }

    @Test
    void shouldImplementSerializable() {
        CreateUserData data = new CreateUserData(
                "admin",
                "password123",
                RoleType.ADMIN
        );

        assertInstanceOf(Serializable.class, data);
    }

    @Test
    void shouldAcceptEveryRoleType() {
        for (RoleType role : RoleType.values()) {
            CreateUserData data = new CreateUserData(
                    "user",
                    "password123",
                    role
            );

            assertEquals(role, data.role());
        }
    }

    @Test
    void shouldRejectNullLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserData(
                        null,
                        "password123",
                        RoleType.ADMIN
                )
        );
    }

    @Test
    void shouldRejectBlankLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserData(
                        "   ",
                        "password123",
                        RoleType.ADMIN
                )
        );
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserData(
                        "admin",
                        null,
                        RoleType.ADMIN
                )
        );
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserData(
                        "admin",
                        "   ",
                        RoleType.ADMIN
                )
        );
    }

    @Test
    void shouldRejectNullRole() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserData(
                        "admin",
                        "password123",
                        null
                )
        );
    }
    @Test
    void shouldHidePasswordInToString() {
        CreateUserData data = new CreateUserData(
                "admin",
                "SecretPassword123",
                RoleType.ADMIN
        );

        String result = data.toString();

        assertFalse(result.contains("SecretPassword123"));
        assertTrue(result.contains("password=***"));
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("ADMIN"));
    }
}
