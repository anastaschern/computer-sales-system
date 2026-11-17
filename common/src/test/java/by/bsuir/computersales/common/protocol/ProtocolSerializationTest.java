
package by.bsuir.computersales.common.protocol;

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
}

