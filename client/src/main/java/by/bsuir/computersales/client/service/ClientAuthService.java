
package by.bsuir.computersales.client.service;

import by.bsuir.computersales.client.network.ClientConnection;
import by.bsuir.computersales.common.dto.LoginRequestData;
import by.bsuir.computersales.common.dto.LoginResult;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.common.protocol.Request;
import by.bsuir.computersales.common.protocol.RequestType;
import by.bsuir.computersales.common.protocol.Response;
import by.bsuir.computersales.common.protocol.ResponseStatus;

import java.io.IOException;

/**
 * Клиентский сервис авторизации.
 *
 * Отвечает за вход пользователя, хранение токена
 * сессии и завершение пользовательской сессии.
 */
public class ClientAuthService implements AutoCloseable {

    private final ClientConnection connection;

    private String sessionToken;
    private User currentUser;

    public ClientAuthService() {
        this.connection = new ClientConnection();
    }

    /**
     * Выполняет авторизацию на сервере.
     */
    public synchronized LoginResult login(
            String host,
            int port,
            String login,
            String password
    ) throws IOException, ClassNotFoundException {

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Введите логин."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Введите пароль."
            );
        }

        // Устанавливаем соединение, если оно ещё не открыто.
        // Закрываем возможное старое соединение.
        connection.close();

// Всегда устанавливаем новое TCP-соединение.
        connection.connect(host, port);

        // Формируем запрос авторизации.
        Request request = new Request(
                RequestType.LOGIN,
                null,
                new LoginRequestData(login, password)
        );

        // Отправляем запрос серверу.
        Response response = connection.sendRequest(request);

        // Проверяем результат авторизации.
        if (response.getStatus() != ResponseStatus.SUCCESS) {
            throw new IllegalStateException(
                    response.getMessage() == null
                            ? "Не удалось выполнить вход."
                            : response.getMessage()
            );
        }

        // Проверяем тип полученных данных.
        if (!(response.getPayload() instanceof LoginResult result)) {
            throw new IllegalStateException(
                    "Сервер вернул некорректный результат авторизации."
            );
        }

        // Сохраняем данные пользовательской сессии.
        this.sessionToken = result.sessionToken();
        this.currentUser = result.user();

        return result;
    }

    /**
     * Завершает пользовательскую сессию.
     */
    public synchronized void logout()
            throws IOException, ClassNotFoundException {

        if (!connection.isConnected() || sessionToken == null) {
            sessionToken = null;
            currentUser = null;
            return;
        }

        Request request = new Request(
                RequestType.LOGOUT,
                sessionToken,
                null
        );

        try {
            Response response = connection.sendRequest(request);

            if (response.getStatus() != ResponseStatus.SUCCESS) {
                throw new IllegalStateException(
                        response.getMessage() == null
                                ? "Не удалось завершить сессию."
                                : response.getMessage()
                );
            }

        } finally {
            sessionToken = null;
            currentUser = null;
        }
    }

    public synchronized String getSessionToken() {
        return sessionToken;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isAuthenticated() {
        return sessionToken != null && currentUser != null;
    }

    /**
     * Закрывает соединение с сервером.
     */
    @Override
    public synchronized void close() {
        connection.disconnect();
        sessionToken = null;
        currentUser = null;
    }
}
