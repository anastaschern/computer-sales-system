
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.database.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleDaoImplTest {

    private RoleDaoImpl createDao(DatabaseManager databaseManager) {
        return new RoleDaoImpl(databaseManager);
    }

    private DatabaseManager createDatabaseManager() throws Exception {
        ServerConfig config = ServerConfig.load(
                Path.of("../server-test.properties")
        );
        return new DatabaseManager(config);
    }

    @Test
    void shouldFindAllRoles() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            List<Role> roles = roleDao.findAll();

            assertEquals(3, roles.size());
            assertTrue(roles.stream().anyMatch(
                    role -> role.getRoleType() == RoleType.ADMIN
            ));
            assertTrue(roles.stream().anyMatch(
                    role -> role.getRoleType() == RoleType.SALES_MANAGER
            ));
            assertTrue(roles.stream().anyMatch(
                    role -> role.getRoleType() == RoleType.INSTALLMENT_SPECIALIST
            ));
        }
    }

    @Test
    void shouldFindRoleById() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            Role admin = roleDao.findByType(RoleType.ADMIN)
                    .orElseThrow();

            Optional<Role> result = roleDao.findById(admin.getRoleId());

            assertTrue(result.isPresent());
            assertEquals(RoleType.ADMIN, result.get().getRoleType());
        }
    }

    @Test
    void shouldReturnEmptyWhenRoleDoesNotExist() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            Optional<Role> result = roleDao.findById(999999L);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void shouldFindRoleByType() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            Optional<Role> result =
                    roleDao.findByType(RoleType.INSTALLMENT_SPECIALIST);

            assertTrue(result.isPresent());
            assertEquals(
                    "Специалист по рассрочке",
                    result.get().getDescription()
            );
        }
    }

    @Test
    void shouldRejectNullRoleType() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            assertThrows(
                    NullPointerException.class,
                    () -> roleDao.findByType(null)
            );
        }
    }

    @Test
    void shouldRejectInvalidRoleId() throws Exception {
        try (DatabaseManager databaseManager = createDatabaseManager()) {
            RoleDaoImpl roleDao = createDao(databaseManager);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> roleDao.findById(0L)
            );
        }
    }
}

