
package by.bsuir.computersales.common.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 * Запрос от клиента к серверу.
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    // Уникальный идентификатор запроса
    private final UUID requestId;

    // Тип операции
    private final RequestType type;

    // Токен пользовательской сессии.
    // При авторизации значение равно null.
    private final String sessionToken;

    // Данные запроса
    private final Serializable payload;

    public Request(
            RequestType type,
            String sessionToken,
            Serializable payload
    ) {
        this.requestId = UUID.randomUUID();
        this.type = type;
        this.sessionToken = sessionToken;
        this.payload = payload;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public RequestType getType() {
        return type;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Serializable getPayload() {
        return payload;
    }
}
