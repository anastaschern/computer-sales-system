
package by.bsuir.computersales.client.server.network;

import by.bsuir.computersales.client.common.dto.LoginRequestData;
import by.bsuir.computersales.client.common.dto.LoginResult;
import by.bsuir.computersales.client.common.model.Role;
import by.bsuir.computersales.client.common.model.RoleType;
import by.bsuir.computersales.client.common.model.User;
import by.bsuir.computersales.client.common.protocol.ErrorCode;
import by.bsuir.computersales.client.common.protocol.Request;
import by.bsuir.computersales.client.common.protocol.RequestType;
import by.bsuir.computersales.client.common.protocol.Response;
import by.bsuir.computersales.client.common.protocol.ResponseStatus;
import by.bsuir.computersales.client.server.dao.UserCredentials;
import by.bsuir.computersales.client.server.dao.UserDao;
import by.bsuir.computersales.client.server.security.PasswordHasher;
import by.bsuir.computersales.client.server.security.SessionManager;
import by.bsuir.computersales.client.server.service.AuthService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerAuthTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    /**
     * Проверяет успешную авторизацию через сетевой протокол.
     */
    @Test
    void shouldLoginSuccessfully() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.LOGIN,
                    null,
                    new LoginRequestData("test-user", "password123")
            );

            Response response = connection.send(request);

            assertEquals(ResponseStatus.SUCCESS, response.getStatus());
            assertNotNull(response.getPayload());
            assertInstanceOf(LoginResult.class, response.getPayload());

            LoginResult result = (LoginResult) response.getPayload();

            assertNotNull(result.sessionToken());
            assertEquals("test-user", result.user().getLogin());

            connection.disconnect();
        }
    }

    /**
     * Проверяет отказ при неправильном пароле.
     */
    @Test
    void shouldRejectLoginWithIncorrectPassword() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.LOGIN,
                    null,
                    new LoginRequestData("test-user", "wrong-password")
            );

            Response response = connection.send(request);

            assertEquals(ResponseStatus.ERROR, response.getStatus());
            assertEquals(
                    ErrorCode.INVALID_CREDENTIALS,
                    response.getErrorCode()
            );

            connection.disconnect();
        }
    }

    /**
     * Проверяет отказ в выполнении запроса без авторизации.
     */
    @Test
    void shouldRejectRequestWithoutSessionToken() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.GET_PRODUCTS,
                    null,
                    null
            );

            Response response = connection.send(request);

            assertEquals(
                    ResponseStatus.UNAUTHORIZED,
                    response.getStatus()
            );

            assertEquals(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    response.getErrorCode()
            );

            connection.disconnect();
        }
    }

    /**
     * Проверяет отказ при использовании недействительного токена.
     */
    @Test
    void shouldRejectRequestWithInvalidSessionToken() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.GET_PRODUCTS,
                    "invalid-session-token",
                    null
            );

            Response response = connection.send(request);

            assertEquals(
                    ResponseStatus.UNAUTHORIZED,
                    response.getStatus()
            );

            assertEquals(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    response.getErrorCode()
            );

            connection.disconnect();
        }
    }

    /**
     * Проверяет, что после выхода токен больше не работает.
     */
    @Test
    void shouldRejectRequestsAfterLogout() throws Exception {
        try (TestConnection connection = openConnection()) {

            // Сначала авторизуемся.
            Response loginResponse = connection.send(
                    new Request(
                            RequestType.LOGIN,
                            null,
                            new LoginRequestData("test-user", "password123")
                    )
            );

            assertEquals(
                    ResponseStatus.SUCCESS,
                    loginResponse.getStatus()
            );

            LoginResult loginResult =
                    (LoginResult) loginResponse.getPayload();

            String sessionToken = loginResult.sessionToken();

            assertNotNull(sessionToken);

            // Завершаем пользовательскую сессию.
            Response logoutResponse = connection.send(
                    new Request(
                            RequestType.LOGOUT,
                            sessionToken,
                            null
                    )
            );

            assertEquals(
                    ResponseStatus.SUCCESS,
                    logoutResponse.getStatus()
            );

            // Повторно используем старый токен.
            Response productsResponse = connection.send(
                    new Request(
                            RequestType.GET_PRODUCTS,
                            sessionToken,
                            null
                    )
            );

            assertEquals(
                    ResponseStatus.UNAUTHORIZED,
                    productsResponse.getStatus()
            );

            assertEquals(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    productsResponse.getErrorCode()
            );

            connection.disconnect();
        }
    }


    /**
     * Создаёт сервис авторизации с тестовым пользователем.
     */
    private AuthService createAuthService() {
        User user = new User(
                1L,
                "test-user",
                new Role(
                        1L,
                        RoleType.ADMIN,
                        "Администратор"
                ),
                true,
                LocalDateTime.now()
        );

        String passwordHash = passwordHasher.hashPassword("password123");

        UserDao userDao = new UserDao() {

            @Override
            public List<User> findAll() {
                return List.of(user);
            }

            @Override
            public Optional<User> findById(Long userId) {
                return user.getUserId().equals(userId)
                        ? Optional.of(user)
                        : Optional.empty();
            }

            @Override
            public Optional<User> findByLogin(String login) {
                return "test-user".equals(login)
                        ? Optional.of(user)
                        : Optional.empty();
            }

            @Override
            public Optional<UserCredentials> findCredentialsByLogin(
                    String login
            ) {
                if (!"test-user".equals(login)) {
                    return Optional.empty();
                }

                return Optional.of(
                        new UserCredentials(user, passwordHash)
                );
            }
        };

        return new AuthService(
                userDao,
                passwordHasher,
                new SessionManager(300)
        );
    }

    /**
     * Открывает тестовое соединение с обработчиком,
     * которому передан сервис авторизации.
     */
    private TestConnection openConnection() throws IOException {
        ServerSocket listener = new ServerSocket(0);

        Socket clientSocket = new Socket(
                "127.0.0.1",
                listener.getLocalPort()
        );

        Socket serverSocket = listener.accept();
        listener.close();

        clientSocket.setSoTimeout(5000);

        Thread handlerThread = new Thread(
                new ClientHandler(serverSocket, createAuthService()),
                "client-handler-auth-test"
        );

        handlerThread.start();

        ObjectOutputStream output = new ObjectOutputStream(
                clientSocket.getOutputStream()
        );
        output.flush();

        ObjectInputStream input = new ObjectInputStream(
                clientSocket.getInputStream()
        );

        return new TestConnection(
                clientSocket,
                handlerThread,
                output,
                input
        );
    }

    /**
     * Хранит ресурсы тестового соединения.
     */
    private static class TestConnection implements AutoCloseable {

        private final Socket clientSocket;
        private final Thread handlerThread;
        private final ObjectOutputStream output;
        private final ObjectInputStream input;

        private TestConnection(
                Socket clientSocket,
                Thread handlerThread,
                ObjectOutputStream output,
                ObjectInputStream input
        ) {
            this.clientSocket = clientSocket;
            this.handlerThread = handlerThread;
            this.output = output;
            this.input = input;
        }

        private Response send(Request request) throws Exception {
            output.writeObject(request);
            output.flush();
            output.reset();

            return (Response) input.readObject();
        }

        private void disconnect() throws Exception {
            Response response = send(
                    new Request(RequestType.DISCONNECT, null, null)
            );

            assertEquals(ResponseStatus.SUCCESS, response.getStatus());
        }

        @Override
        public void close() throws Exception {
            clientSocket.close();
            handlerThread.join(3000);

            if (handlerThread.isAlive()) {
                handlerThread.interrupt();
            }
        }
    }
}
