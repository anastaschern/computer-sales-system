
package by.bsuir.computersales.server.network;

import by.bsuir.computersales.common.protocol.ErrorCode;
import by.bsuir.computersales.common.protocol.Request;
import by.bsuir.computersales.common.protocol.RequestType;
import by.bsuir.computersales.common.protocol.Response;
import by.bsuir.computersales.common.protocol.ResponseStatus;
import org.junit.jupiter.api.Test;
import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.dto.SaleItemData;
import by.bsuir.computersales.common.model.PaymentType;

import java.util.List;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientHandlerTest {

    /**
     * Проверяет обработку запроса на отключение клиента.
     */
    @Test
    void shouldDisconnectClientSuccessfully() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.DISCONNECT,
                    null,
                    null
            );

            connection.output.writeObject(request);
            connection.output.flush();

            Response response = (Response) connection.input.readObject();

            assertNotNull(response);
            assertEquals(
                    request.getRequestId(),
                    response.getRequestId()
            );
            assertEquals(
                    ResponseStatus.SUCCESS,
                    response.getStatus()
            );
            assertEquals(
                    "Соединение завершено.",
                    response.getMessage()
            );

            connection.handlerThread.join(3000);

            assertFalse(
                    connection.handlerThread.isAlive(),
                    "Поток обработки клиента должен завершиться"
            );
        }
    }

    /**
     * Проверяет ответ сервера на неподдерживаемый запрос.
     */
    @Test
    void shouldReturnErrorForUnimplementedRequest() throws Exception {
        try (TestConnection connection = openConnection()) {

            Request request = new Request(
                    RequestType.GET_PRODUCTS,
                    null,
                    "product-filter"
            );

            connection.output.writeObject(request);
            connection.output.flush();

            Response response = (Response) connection.input.readObject();

            assertNotNull(response);
            assertEquals(
                    request.getRequestId(),
                    response.getRequestId()
            );
            assertEquals(
                    ResponseStatus.ERROR,
                    response.getStatus()
            );
            assertEquals(
                    ErrorCode.BUSINESS_RULE,
                    response.getErrorCode()
            );
            assertEquals(
                    "Обработка данного запроса пока не реализована.",
                    response.getMessage()
            );

            // Отправляем запрос отключения,
            // чтобы корректно завершить обработчик.
            Request disconnectRequest = new Request(
                    RequestType.DISCONNECT,
                    null,
                    null
            );

            connection.output.writeObject(disconnectRequest);
            connection.output.flush();

            Response disconnectResponse =
                    (Response) connection.input.readObject();

            assertEquals(
                    ResponseStatus.SUCCESS,
                    disconnectResponse.getStatus()
            );

            connection.handlerThread.join(3000);

            assertFalse(
                    connection.handlerThread.isAlive(),
                    "Поток обработки клиента должен завершиться"
            );
        }
    }

    /**
     * Проверяет, что сервер принимает DTO продажи
     * через фильтр десериализации.
     */
    @Test
    void shouldAcceptCreateSaleDataThroughInputFilter() throws Exception {
        try (TestConnection connection = openConnection()) {

            CreateSaleData saleData = new CreateSaleData(
                    10L,
                    PaymentType.FULL_PAYMENT,
                    List.of(
                            new SaleItemData(1L, 2),
                            new SaleItemData(2L, 3)
                    )
            );

            Request request = new Request(
                    RequestType.CREATE_SALE,
                    null,
                    saleData
            );

            connection.output.writeObject(request);
            connection.output.flush();

            Response response =
                    (Response) connection.input.readObject();

            assertNotNull(response);

            assertEquals(
                    request.getRequestId(),
                    response.getRequestId()
            );

            // BUSINESS_RULE означает, что объект был принят,
            // десериализован и дошёл до обработки запроса.
            assertEquals(
                    ResponseStatus.ERROR,
                    response.getStatus()
            );

            assertEquals(
                    ErrorCode.BUSINESS_RULE,
                    response.getErrorCode()
            );

            // Корректно завершаем соединение.
            Request disconnectRequest = new Request(
                    RequestType.DISCONNECT,
                    null,
                    null
            );

            connection.output.writeObject(disconnectRequest);
            connection.output.flush();

            Response disconnectResponse =
                    (Response) connection.input.readObject();

            assertEquals(
                    ResponseStatus.SUCCESS,
                    disconnectResponse.getStatus()
            );

            connection.handlerThread.join(3000);

            assertFalse(
                    connection.handlerThread.isAlive(),
                    "Поток обработки клиента должен завершиться"
            );
        }
    }


    /**
     * Проверяет защиту конструктора от null вместо сокета.
     */
    @Test
    void shouldRejectNullSocket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClientHandler(null)
        );

    }

    /**
     * Создаёт пару локальных сокетов и запускает обработчик.
     */
    private TestConnection openConnection() throws IOException {
        ServerSocket listener = new ServerSocket(0);

        Socket clientSocket = new Socket(
                "127.0.0.1",
                listener.getLocalPort()
        );

        Socket serverSocket = listener.accept();
        listener.close();

        clientSocket.setSoTimeout(3000);

        Thread handlerThread = new Thread(
                new ClientHandler(serverSocket),
                "client-handler-test"
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
     * Хранит ресурсы тестового сетевого соединения.
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
