
package by.bsuir.computersales.server.security;

import by.bsuir.computersales.server.exception.AuthenticationException;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Управляет пользовательскими сессиями.
 *
 * Связывает токен сессии с идентификатором
 * авторизованного пользователя.
 */
public class SessionManager {

    private final ConcurrentMap<String, Session> sessions =
            new ConcurrentHashMap<>();

    private final long timeoutMillis;

    /**
     * Создаёт менеджер сессий.
     *
     * @param timeoutSeconds время жизни сессии в секундах
     */
    public SessionManager(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Время жизни сессии должно быть положительным"
            );
        }

        this.timeoutMillis = timeoutSeconds * 1000L;
    }

    /**
     * Создаёт новую сессию пользователя.
     *
     * @param userId идентификатор пользователя
     * @return токен созданной сессии
     */
    public String createSession(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор пользователя должен быть положительным"
            );
        }

        String token = UUID.randomUUID().toString();

        sessions.put(
                token,
                new Session(userId, System.currentTimeMillis())
        );

        return token;
    }

    /**
     * Получает идентификатор пользователя по токену.
     *
     * При обращении к действующей сессии
     * обновляет время последней активности.
     *
     * @param token токен пользовательской сессии
     * @return идентификатор пользователя
     */
    public Long getUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException(
                    "Необходимо выполнить вход в систему"
            );
        }

        Session session = sessions.get(token);

        if (session == null) {
            throw new AuthenticationException(
                    "Сессия недействительна или завершена"
            );
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - session.lastActivityTime > timeoutMillis) {
            sessions.remove(token, session);

            throw new AuthenticationException(
                    "Время действия сессии истекло"
            );
        }

        session.lastActivityTime = currentTime;

        return session.userId;
    }

    /**
     * Завершает пользовательскую сессию.
     *
     * @param token токен сессии
     */
    public void invalidateSession(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    /**
     * Завершает все активные сессии.
     */
    public void invalidateAll() {
        sessions.clear();
    }

    /**
     * Возвращает количество зарегистрированных сессий.
     *
     * @return количество сессий
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Данные одной пользовательской сессии.
     */
    private static class Session {

        private final Long userId;

        private volatile long lastActivityTime;

        private Session(Long userId, long lastActivityTime) {
            this.userId = userId;
            this.lastActivityTime = lastActivityTime;
        }
    }
}
