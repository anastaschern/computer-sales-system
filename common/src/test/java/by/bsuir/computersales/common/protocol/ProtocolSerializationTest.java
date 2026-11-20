
package by.bsuir.computersales.common.protocol;

import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.dto.SaleItemData;
import by.bsuir.computersales.common.model.PaymentType;

import java.util.List;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверка сериализации и десериализации сетевых объектов.
 */
class ProtocolSerializationTest {

    /**
     * Преобразует объект в массив байтов,
     * а затем восстанавливает его.
     */
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T serializeAndRestore(T object)
            throws IOException, ClassNotFoundException {

        ByteArrayOutputStream byteStream =
                new ByteArrayOutputStream();

        try (ObjectOutputStream output =
                     new ObjectOutputStream(byteStream)) {
            output.writeObject(object);
        }

        byte[] data = byteStream.toByteArray();

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new ByteArrayInputStream(data))) {
            return (T) input.readObject();
        }
    }

    @Test
    void requestShouldBeSerializedAndRestored()
            throws Exception {

        Request original = new Request(
                RequestType.GET_PRODUCTS,
                "test-session-token",
                "product-filter"
        );

        Request restored = serializeAndRestore(original);

        assertEquals(original.getRequestId(), restored.getRequestId());
        assertEquals(original.getType(), restored.getType());
        assertEquals(
                original.getSessionToken(),
                restored.getSessionToken()
        );
        assertEquals(original.getPayload(), restored.getPayload());
    }

    @Test
    void responseShouldBeSerializedAndRestored()
            throws Exception {

        Response original = Response.error(
                java.util.UUID.randomUUID(),
                ErrorCode.VALIDATION,
                "Некорректные данные"
        );

        Response restored = serializeAndRestore(original);

        assertEquals(original.getRequestId(), restored.getRequestId());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getErrorCode(), restored.getErrorCode());
        assertEquals(original.getMessage(), restored.getMessage());
        assertNull(restored.getPayload());
    }

    @Test
    void responseShouldRejectNullRequestId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Response(
                        null,
                        ResponseStatus.SUCCESS,
                        null,
                        "Успешно",
                        null
                )
        );
    }

    @Test
    void responseShouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Response(
                        java.util.UUID.randomUUID(),
                        null,
                        null,
                        "Ответ",
                        null
                )
        );
    }

    @Test
    void successfulResponseShouldRejectErrorCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Response(
                        java.util.UUID.randomUUID(),
                        ResponseStatus.SUCCESS,
                        ErrorCode.INTERNAL,
                        "Успешно",
                        null
                )
        );
    }

    @Test
    void errorResponseShouldRequireErrorCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Response(
                        java.util.UUID.randomUUID(),
                        ResponseStatus.ERROR,
                        null,
                        "Ошибка",
                        null
                )
        );
    }
    @Test
    void requestShouldRejectNullType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Request(
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void eachRequestShouldHaveUniqueId() {
        Request firstRequest = new Request(
                RequestType.GET_PRODUCTS,
                null,
                null
        );

        Request secondRequest = new Request(
                RequestType.GET_PRODUCTS,
                null,
                null
        );

        assertNotEquals(
                firstRequest.getRequestId(),
                secondRequest.getRequestId()
        );
    }
    @Test
    void createSaleDataShouldBeSerializedAndRestored()
            throws Exception {

        CreateSaleData original = new CreateSaleData(
                10L,
                PaymentType.FULL_PAYMENT,
                List.of(
                        new SaleItemData(1L, 2),
                        new SaleItemData(2L, 3)
                )
        );

        CreateSaleData restored = serializeAndRestore(original);

        assertEquals(original.customerId(), restored.customerId());
        assertEquals(original.paymentType(), restored.paymentType());

        assertEquals(2, restored.items().size());

        assertEquals(1L, restored.items().get(0).productId());
        assertEquals(2, restored.items().get(0).quantity());

        assertEquals(2L, restored.items().get(1).productId());
        assertEquals(3, restored.items().get(1).quantity());
    }
}

