
package by.bsuir.computersales.client.server.service;

import by.bsuir.computersales.client.common.dto.LoginResult;
import by.bsuir.computersales.client.common.model.User;
import by.bsuir.computersales.client.common.protocol.ErrorCode;
import by.bsuir.computersales.client.server.dao.UserCredentials;
import by.bsuir.computersales.client.server.dao.UserDao;
import by.bsuir.computersales.client.server.exception.AuthenticationException;
import by.bsuir.computersales.client.server.security.PasswordHasher;
import by.bsuir.computersales.client.server.security.SessionManager;

import java.util.Objects;
import java.util.Optional;

/**
 * Сервис аутентификации пользователей системы.
 *
 * Отвечает за проверку учётных данных,
 * создание пользовательских сессий
 * и завершение авторизации.
 */
public class AuthService {

    private final UserDao userDao;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    /**
     * Создаёт сервис аутентификации.
     *
     * @param userDao доступ к данным пользователей
     * @param passwordHasher средство проверки паролей
     * @param sessionManager менеджер пользовательских сессий
     */
    public AuthService(
            UserDao userDao,
            PasswordHasher passwordHasher,
            SessionManager sessionManager
    ) {
        this.userDao = Objects.requireNonNull(
                userDao,
                "DAO пользователей не должен быть null"
        );

        this.passwordHasher = Objects.requireNonNull(
                passwordHasher,
                "Средство проверки паролей не должно быть null"
        );

        this.sessionManager = Objects.requireNonNull(
                sessionManager,
                "Менеджер сессий не должен быть null"
        );
    }

    /**
     * Выполняет авторизацию пользователя.
     *
     * Проверяет логин и пароль, убеждается,
     * что учётная запись активна, после чего
     * создаёт пользовательскую сессию.
     *
     * @param login логин пользователя
     * @param password пароль пользователя
     * @return результат успешной авторизации
     * @throws AuthenticationException при неверных учётных данных
     */
    public LoginResult login(String login, String password) {
        if (login == null || login.isBlank()
                || password == null || password.isEmpty()) {
            throw new AuthenticationException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Неверный логин или пароль"
            );
        }

        Optional<UserCredentials> optionalCredentials =
                userDao.findCredentialsByLogin(login);

        if (optionalCredentials.isEmpty()) {
            throw invalidCredentials();
        }

        UserCredentials credentials = optionalCredentials.get();

        User user = credentials.user();

        boolean passwordCorrect = passwordHasher.verifyPassword(
                password,
                credentials.passwordHash()
        );

        if (!passwordCorrect || !user.isActive()) {
            throw invalidCredentials();
        }

        String sessionToken = sessionManager.createSession(
                user.getUserId()
        );

        return new LoginResult(sessionToken, user);
    }

    /**
     * Завершает пользовательскую сессию.
     *
     * @param sessionToken токен сессии
     */
    public void logout(String sessionToken) {
        sessionManager.invalidateSession(sessionToken);
    }

    /**
     * Получает идентификатор авторизованного пользователя.
     *
     * При проверке действительности токена
     * обновляется время активности сессии.
     *
     * @param sessionToken токен сессии
     * @return идентификатор пользователя
     */
    public Long getAuthenticatedUserId(String sessionToken) {
        return sessionManager.getUserId(sessionToken);
    }

    /**
     * Создаёт единообразную ошибку авторизации.
     *
     * Не раскрывает, существует ли пользователь
     * и правильно ли введён пароль.
     *
     * @return исключение с общим сообщением
     */
    private AuthenticationException invalidCredentials() {
        return new AuthenticationException(
                ErrorCode.INVALID_CREDENTIALS,
                "Неверный логин или пароль"
        );
    }
}
