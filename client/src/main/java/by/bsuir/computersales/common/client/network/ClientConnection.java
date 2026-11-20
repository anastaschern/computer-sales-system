
package by.bsuir.computersales.client.network;

import by.bsuir.computersales.client.common.protocol.Request;
import by.bsuir.computersales.client.common.protocol.RequestType;
import by.bsuir.computersales.client.common.protocol.Response;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Управляет сетевым соединением клиентского приложения с сервером.
 *
 * Отвечает за:
 * - установление соединения;
 * - отправку запросов;
 * - получение ответов;
 * - корректное завершение соединения.
 */
public class ClientConnection implements AutoCloseable {

    private static final int CONNECTION_TIMEOUT = 5000;

    private static final long MAX_DEPTH = 20;
    private static final long MAX_REFERENCES = 1000;
    private static final long MAX_BYTES = 1_048_576;
    private static final long MAX_ARRAY_LENGTH = 10_000;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    /**
     * Устанавливает соединение с сервером.
     *
     * @param host адрес сервера
     * @param port порт сервера
     * @throws IOException если соединение установить не удалось
     */
    public synchronized void connect(String host, int port)
            throws IOException {

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                    "Адрес сервера не может быть пустым."
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Некорректный номер порта."
            );
        }

        close();

        Socket newSocket = new Socket();

        try {
            // Устанавливаем TCP-соединение с сервером.
            newSocket.connect(
                    new InetSocketAddress(host, port),
                    CONNECTION_TIMEOUT
            );

            // Сначала создаём поток вывода.
            ObjectOutputStream newOutput =
                    new ObjectOutputStream(newSocket.getOutputStream());

            newOutput.flush();

            // Затем создаём поток ввода.
            ObjectInputStream newInput =
                    new ObjectInputStream(newSocket.getInputStream());

            // Устанавливаем фильтр входящих объектов.
            newInput.setObjectInputFilter(createInputFilter());

            this.socket = newSocket;
            this.output = newOutput;
            this.input = newInput;

        } catch (IOException | RuntimeException e) {

            try {
                newSocket.close();
            } catch (IOException closeException) {
                e.addSuppressed(closeException);
            }

            throw e;
        }
    }

    /**
     * Отправляет запрос серверу и получает ответ.
     *
     * Метод synchronized гарантирует, что два запроса
     * не будут одновременно использовать одни потоки.
     *
     * @param request запрос серверу
     * @return ответ сервера
     * @throws IOException при ошибке сетевого взаимодействия
     * @throws ClassNotFoundException если получен неизвестный класс
     */
    public synchronized Response sendRequest(Request request)
            throws IOException, ClassNotFoundException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Запрос не может быть null."
            );
        }

        if (!isConnected()) {
            throw new IOException(
                    "Соединение с сервером отсутствует."
            );
        }

        // Отправляем запрос.
        output.writeObject(request);
        output.flush();
        output.reset();

        // Получаем ответ.
        Object receivedObject = input.readObject();

        if (!(receivedObject instanceof Response response)) {
            throw new InvalidClassException(
                    "Сервер вернул объект неожиданного типа."
            );
        }

        // Проверяем соответствие ответа отправленному запросу.
        if (!request.getRequestId().equals(response.getRequestId())) {
            throw new IOException(
                    "Идентификатор ответа не соответствует запросу."
            );
        }

        return response;
    }

    /**
     * Проверяет наличие активного соединения.
     *
     * @return true, если соединение активно
     */
    public synchronized boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    /**
     * Завершает соединение с сервером,
     * предварительно отправляя запрос DISCONNECT.
     */
    public synchronized void disconnect() {

        if (isConnected()) {
            try {
                Request request = new Request(
                        RequestType.DISCONNECT,
                        null,
                        null
                );

                sendRequest(request);

            } catch (IOException | ClassNotFoundException e) {
                System.err.println(
                        "Ошибка при завершении соединения: "
                                + e.getMessage()
                );

            } finally {
                close();
            }
        } else {
            close();
        }
    }

    /**
     * Создаёт фильтр десериализации входящих объектов.
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

            String className = serialClass.getName();

            // Разрешаем классы общего модуля проекта.
            if (className.startsWith(
                    "by.bsuir.computersales.client.common."
            )) {
                return ObjectInputFilter.Status.ALLOWED;
            }

            // Разрешаем необходимые классы Java.
            if (serialClass == String.class
                    || serialClass == Long.class
                    || serialClass == Integer.class
                    || serialClass == Boolean.class
                    || serialClass == Number.class
                    || serialClass == Enum.class
                    || serialClass == Object[].class
                    || serialClass == java.util.ArrayList.class
                    || serialClass == java.util.UUID.class
                    || serialClass == java.math.BigDecimal.class
                    || serialClass == java.time.Ser.class) {

                return ObjectInputFilter.Status.ALLOWED;
            }

            // Остальные классы запрещены.
            return ObjectInputFilter.Status.REJECTED;
        };
    }

    /**
     * Закрывает сетевые потоки и сокет.
     */
    @Override
    public synchronized void close() {

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Ошибка закрытия входящего потока: "
                            + e.getMessage()
            );
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Ошибка закрытия исходящего потока: "
                            + e.getMessage()
            );
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Ошибка закрытия сокета: "
                            + e.getMessage()
            );
        }

        input = null;
        output = null;
        socket = null;
    }
}
