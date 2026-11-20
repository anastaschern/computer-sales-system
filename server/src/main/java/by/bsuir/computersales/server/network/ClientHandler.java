
package by.bsuir.computersales.server.network;

import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.dto.CreateUserData;
import by.bsuir.computersales.common.dto.LoginRequestData;
import by.bsuir.computersales.common.dto.LoginResult;
import by.bsuir.computersales.common.dto.SaleItemData;
import by.bsuir.computersales.common.model.PaymentType;
import by.bsuir.computersales.common.protocol.ErrorCode;
import by.bsuir.computersales.common.protocol.Request;
import by.bsuir.computersales.common.protocol.RequestType;
import by.bsuir.computersales.common.protocol.Response;
import by.bsuir.computersales.server.exception.AppException;
import by.bsuir.computersales.server.exception.AuthenticationException;
import by.bsuir.computersales.server.service.AuthService;

import java.util.ArrayList;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;

/**
 * Обрабатывает соединение с одним клиентом.
 *
 * Каждый экземпляр ClientHandler выполняется
 * в отдельном потоке серверного пула.
 */
public class ClientHandler implements Runnable {

    private static final long MAX_DEPTH = 20;
    private static final long MAX_REFERENCES = 1_000;
    private static final long MAX_BYTES = 1_048_576;
    private static final long MAX_ARRAY_LENGTH = 10_000;

    private final Socket clientSocket;
    private final AuthService authService;

    private volatile boolean running = true;

    /**
     * Создаёт обработчик без сервиса авторизации.
     *
     * Используется для обратной совместимости
     * с существующими сетевыми тестами.
     */
    public ClientHandler(Socket clientSocket) {
        this(clientSocket, null);
    }

    /**
     * Создаёт обработчик с сервисом авторизации.
     *
     * @param clientSocket сокет клиента
     * @param authService сервис авторизации
     */
    public ClientHandler(Socket clientSocket, AuthService authService) {
        if (clientSocket == null) {
            throw new IllegalArgumentException(
                    "Сокет клиента не должен быть null"
            );
        }

        this.clientSocket = clientSocket;
        this.authService = authService;
    }

    @Override
    public void run() {
        try (
                Socket socket = clientSocket;
                ObjectOutputStream output =
                        new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input =
                        new ObjectInputStream(socket.getInputStream())
        ) {
            input.setObjectInputFilter(createInputFilter());

            System.out.println(
                    "Начата обработка клиента: "
                            + socket.getRemoteSocketAddress()
            );

            while (running && !socket.isClosed()) {
                Object receivedObject;

                try {
                    receivedObject = input.readObject();
                } catch (EOFException | SocketException e) {
                    break;
                }

                if (!(receivedObject instanceof Request request)) {
                    break;
                }

                Response response = processRequest(request);

                output.writeObject(response);
                output.flush();
                output.reset();

                if (request.getType() == RequestType.DISCONNECT) {
                    break;
                }
            }

        } catch (InvalidClassException e) {
            System.err.println(
                    "Отклонён недопустимый объект от клиента: "
                            + e.getMessage()
            );
        } catch (IOException e) {
            System.err.println(
                    "Ошибка соединения с клиентом: "
                            + e.getMessage()
            );
        } catch (ClassNotFoundException e) {
            System.err.println(
                    "Получен неизвестный класс объекта."
            );
        } finally {
            running = false;

            System.out.println(
                    "Соединение с клиентом закрыто."
            );
        }
    }

    /**
     * Создаёт фильтр входящих сериализованных объектов.
     */
    private ObjectInputFilter createInputFilter() {
        return filterInfo -> {
            if (filterInfo.depth() > MAX_DEPTH
                    || filterInfo.references() > MAX_REFERENCES
                    || filterInfo.streamBytes() > MAX_BYTES
                    || filterInfo.arrayLength() > MAX_ARRAY_LENGTH) {
                return ObjectInputFilter.Status.REJECTED;
            }

            Class<?> serialClass = filterInfo.serialClass();

            if (serialClass == null) {
                return ObjectInputFilter.Status.UNDECIDED;
            }

            if (serialClass == Request.class
                    || serialClass == RequestType.class
                    || serialClass == String.class
                    || serialClass == java.util.UUID.class
                    || serialClass == Enum.class
                    || serialClass.isEnum()
                    || serialClass == LoginRequestData.class
                    || serialClass == CreateUserData.class
                    || serialClass == CreateSaleData.class
                    || serialClass == SaleItemData.class
                    || serialClass == PaymentType.class
                    || serialClass == Long.class
                    || serialClass == Number.class
                    || serialClass == ArrayList.class
                    || serialClass == Object[].class) {

                return ObjectInputFilter.Status.ALLOWED;
            }

            return ObjectInputFilter.Status.REJECTED;
        };
    }

    /**
     * Обрабатывает запрос клиента.
     */
    private Response processRequest(Request request) {
        if (request.getType() == RequestType.DISCONNECT) {
            running = false;

            return Response.success(
                    request.getRequestId(),
                    "Соединение завершено.",
                    null
            );
        }

        // Старый конструктор оставлен для совместимости
        // с существующими тестами ClientHandler.
        if (authService == null) {
            return Response.error(
                    request.getRequestId(),
                    ErrorCode.BUSINESS_RULE,
                    "Обработка данного запроса пока не реализована."
            );
        }

        try {
            return switch (request.getType()) {
                case LOGIN -> processLogin(request);
                case LOGOUT -> processLogout(request);
                default -> processAuthenticatedRequest(request);
            };

        } catch (AuthenticationException e) {
            if (e.getErrorCode() == ErrorCode.AUTHENTICATION_REQUIRED) {
                return Response.unauthorized(
                        request.getRequestId(),
                        e.getErrorCode(),
                        e.getMessage()
                );
            }

            return Response.error(
                    request.getRequestId(),
                    e.getErrorCode(),
                    e.getMessage()
            );

        } catch (AppException e) {
            return Response.error(
                    request.getRequestId(),
                    e.getErrorCode(),
                    e.getMessage()
            );
        }
    }

    /**
     * Выполняет вход пользователя.
     */
    private Response processLogin(Request request) {
        if (!(request.getPayload() instanceof LoginRequestData loginData)) {
            return Response.error(
                    request.getRequestId(),
                    ErrorCode.VALIDATION,
                    "Переданы некорректные данные для авторизации."
            );
        }

        LoginResult loginResult = authService.login(
                loginData.login(),
                loginData.password()
        );

        return Response.success(
                request.getRequestId(),
                "Авторизация выполнена успешно.",
                loginResult
        );
    }

    /**
     * Завершает пользовательскую сессию.
     */
    private Response processLogout(Request request) {
        String sessionToken = request.getSessionToken();

        if (sessionToken == null || sessionToken.isBlank()) {
            throw new AuthenticationException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "Для завершения сессии необходимо авторизоваться."
            );
        }

        authService.getAuthenticatedUserId(sessionToken);
        authService.logout(sessionToken);

        return Response.success(
                request.getRequestId(),
                "Сеанс пользователя завершён.",
                null
        );
    }

    /**
     * Проверяет авторизацию перед обработкой остальных запросов.
     *
     * Идентификатор пользователя извлекается из серверной сессии.
     */
    private Response processAuthenticatedRequest(Request request) {
        String sessionToken = request.getSessionToken();

        if (sessionToken == null || sessionToken.isBlank()) {
            throw new AuthenticationException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "Для выполнения операции необходимо авторизоваться."
            );
        }

        Long authenticatedUserId =
                authService.getAuthenticatedUserId(sessionToken);

        // На следующих этапах здесь будет подключаться
        // бизнес-логика с использованием authenticatedUserId.

        Objects.requireNonNull(
                authenticatedUserId,
                "Идентификатор пользователя не должен быть null"
        );

        return Response.error(
                request.getRequestId(),
                ErrorCode.BUSINESS_RULE,
                "Обработка данного запроса пока не реализована."
        );
    }
}
