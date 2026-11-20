
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.RoleType;
import by.bsuir.computersales.client.common.model.User;
import by.bsuir.computersales.client.server.config.ServerConfig;
import by.bsuir.computersales.client.server.database.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest {

    private static DatabaseManager databaseManager;

    private UserDao userDao;

    @BeforeAll
    static void setUpDatabase() {
        ServerConfig config = ServerConfig.load(Path.of("../server.properties"));
        databaseManager = new DatabaseManager(config);
    }

    @AfterAll
    static void closeDatabase() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(databaseManager);
    }

    @Test
    void findAllShouldReturnAllUsers() {
        List<User> users = userDao.findAll();

        assertEquals(3, users.size());

        assertTrue(users.stream().anyMatch(user ->
                user.getRole().getRoleType() == RoleType.ADMIN));

        assertTrue(users.stream().anyMatch(user ->
                user.getRole().getRoleType() == RoleType.SALES_MANAGER));

        assertTrue(users.stream().anyMatch(user ->
                user.getRole().getRoleType() == RoleType.INSTALLMENT_SPECIALIST));
    }

    @Test
    void findByIdShouldReturnUserWhenUserExists() {
        List<User> users = userDao.findAll();

        Long userId = users.get(0).getUserId();

        Optional<User> result = userDao.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userDao.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByLoginShouldReturnUserWhenUserExists() {
        List<User> users = userDao.findAll();

        String login = users.get(0).getLogin();

        Optional<User> result = userDao.findByLogin(login);

        assertTrue(result.isPresent());
        assertEquals(login, result.get().getLogin());
    }

    @Test
    void findByLoginShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userDao.findByLogin("unknown_user_999");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findById(null)
        );
    }

    @Test
    void findByIdShouldRejectZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findById(0L)
        );
    }

    @Test
    void findByLoginShouldRejectNullLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findByLogin(null)
        );
    }

    @Test
    void findByLoginShouldRejectBlankLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findByLogin("   ")
        );
    }

    @Test
    void findAllShouldNotExposePasswordHash() {
        List<User> users = userDao.findAll();

        assertFalse(users.isEmpty());

        for (User user : users) {
            assertNotNull(user.getLogin());
            assertNotNull(user.getRole());
            assertNotNull(user.getCreatedAt());
        }
    }

    @Test
    void findCredentialsByLoginShouldReturnCredentialsWhenUserExists() {
        List<User> users = userDao.findAll();
        String login = users.get(0).getLogin();

        Optional<UserCredentials> result =
                userDao.findCredentialsByLogin(login);

        assertTrue(result.isPresent());

        UserCredentials credentials = result.get();

        assertEquals(login, credentials.user().getLogin());
        assertNotNull(credentials.passwordHash());
        assertFalse(credentials.passwordHash().isBlank());
    }

    @Test
    void findCredentialsByLoginShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<UserCredentials> result =
                userDao.findCredentialsByLogin("unknown_user_999");

        assertTrue(result.isEmpty());
    }

    @Test
    void findCredentialsByLoginShouldRejectNullLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findCredentialsByLogin(null)
        );
    }

    @Test
    void findCredentialsByLoginShouldRejectBlankLogin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userDao.findCredentialsByLogin("   ")
        );
    }
}
