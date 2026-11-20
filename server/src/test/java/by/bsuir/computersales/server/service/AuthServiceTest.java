
package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.dto.LoginResult;
import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.server.dao.UserCredentials;
import by.bsuir.computersales.server.dao.UserDao;
import by.bsuir.computersales.server.exception.AuthenticationException;
import by.bsuir.computersales.server.security.PasswordHasher;
import by.bsuir.computersales.server.security.SessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String LOGIN = "test_user";
    private static final String PASSWORD = "TestPassword123";

    private static PasswordHasher passwordHasher;
    private static String passwordHash;

    private FakeUserDao userDao;
    private SessionManager sessionManager;
    private AuthService authService;

    /**
     * Создаёт хеш пароля один раз для всех тестов.
     */
    @BeforeAll
    static void setUpPasswordHash() {
        passwordHasher = new PasswordHasher();
        passwordHash = passwordHasher.hashPassword(PASSWORD);
    }

    /**
     * Подготавливает зависимости перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userDao = new FakeUserDao(createUser(true), passwordHash);
        sessionManager = new SessionManager(3600);

        authService = new AuthService(
                userDao,
                passwordHasher,
                sessionManager
        );
    }

    @Test
    void loginShouldReturnLoginResultWhenCredentialsAreCorrect() {
        LoginResult result = authService.login(LOGIN, PASSWORD);

        assertNotNull(result);
        assertNotNull(result.sessionToken());
        assertFalse(result.sessionToken().isBlank());

        assertEquals(LOGIN, result.user().getLogin());
        assertTrue(result.user().isActive());

        assertEquals(
                result.user().getUserId(),
                sessionManager.getUserId(result.sessionToken())
        );
    }

    @Test
    void loginShouldRejectIncorrectPassword() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(LOGIN, "WrongPassword123")
        );

        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    void loginShouldRejectUnknownUser() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login("unknown_user", PASSWORD)
        );

        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    void loginShouldRejectInactiveUser() {
        userDao.setCredentials(
                createUser(false),
                passwordHash
        );

        assertThrows(
                AuthenticationException.class,
                () -> authService.login(LOGIN, PASSWORD)
        );

        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    void loginShouldRejectNullLogin() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(null, PASSWORD)
        );
    }

    @Test
    void loginShouldRejectBlankLogin() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login("   ", PASSWORD)
        );
    }

    @Test
    void loginShouldRejectNullPassword() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(LOGIN, null)
        );
    }

    @Test
    void loginShouldRejectEmptyPassword() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(LOGIN, "")
        );
    }

    @Test
    void logoutShouldInvalidateSession() {
        LoginResult result = authService.login(LOGIN, PASSWORD);

        assertEquals(1, sessionManager.getActiveSessionCount());

        authService.logout(result.sessionToken());

        assertEquals(0, sessionManager.getActiveSessionCount());

        assertThrows(
                AuthenticationException.class,
                () -> authService.getAuthenticatedUserId(
                        result.sessionToken()
                )
        );
    }

    @Test
    void getAuthenticatedUserIdShouldReturnCorrectUserId() {
        LoginResult result = authService.login(LOGIN, PASSWORD);

        Long userId = authService.getAuthenticatedUserId(
                result.sessionToken()
        );

        assertEquals(result.user().getUserId(), userId);
    }

    @Test
    void getAuthenticatedUserIdShouldRejectInvalidToken() {
        assertThrows(
                AuthenticationException.class,
                () -> authService.getAuthenticatedUserId("invalid-token")
        );
    }

    /**
     * Создаёт тестового пользователя.
     *
     * @param active признак активности учётной записи
     * @return тестовый пользователь
     */
    private User createUser(boolean active) {
        return new User(
                1L,
                LOGIN,
                new Role(
                        1L,
                        RoleType.ADMIN,
                        "Администратор"
                ),
                active,
                LocalDateTime.now()
        );
    }

    /**
     * Тестовая реализация DAO пользователей.
     *
     * Не обращается к базе данных.
     */
    private static class FakeUserDao implements UserDao {

        private UserCredentials credentials;

        private FakeUserDao(User user, String hash) {
            setCredentials(user, hash);
        }

        private void setCredentials(User user, String hash) {
            credentials = new UserCredentials(user, hash);
        }

        @Override
        public List<User> findAll() {
            return List.of(credentials.user());
        }

        @Override
        public Optional<User> findById(Long userId) {
            if (credentials.user().getUserId().equals(userId)) {
                return Optional.of(credentials.user());
            }

            return Optional.empty();
        }

        @Override
        public Optional<User> findByLogin(String login) {
            if (credentials.user().getLogin().equals(login)) {
                return Optional.of(credentials.user());
            }

            return Optional.empty();
        }

        @Override
        public Optional<UserCredentials> findCredentialsByLogin(
                String login
        ) {
            if (credentials.user().getLogin().equals(login)) {
                return Optional.of(credentials);
            }

            return Optional.empty();
        }
    }
}
