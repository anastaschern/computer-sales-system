
package by.bsuir.computersales.server.network;

import by.bsuir.computersales.common.protocol.ErrorCode;
import by.bsuir.computersales.common.protocol.Request;
import by.bsuir.computersales.common.protocol.Response;
import by.bsuir.computersales.common.protocol.RequestType;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

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

    private volatile boolean running = true;

    public ClientHandler(Socket clientSocket) {
        if (clientSocket == null) {
            throw new IllegalArgumentException(
                    "Сокет клиента не должен быть null"
            );
        }

        this.clientSocket = clientSocket;
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
                    || serialClass.isEnum()) {
                return ObjectInputFilter.Status.ALLOWED;
            }

            return ObjectInputFilter.Status.REJECTED;
        };
    }


    /**
     * Обрабатывает запрос клиента.
     *
     * Реальная бизнес-логика будет подключена
     * после создания соответствующих сервисов.
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

        return Response.error(
                request.getRequestId(),
                ErrorCode.BUSINESS_RULE,
                "Обработка данного запроса пока не реализована."
        );
    }
}
