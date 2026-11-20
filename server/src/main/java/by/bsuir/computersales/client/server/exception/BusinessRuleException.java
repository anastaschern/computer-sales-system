
package by.bsuir.computersales.client.server.exception;

import by.bsuir.computersales.client.common.protocol.ErrorCode;

/**
 * Исключение, возникающее при нарушении бизнес-правил системы.
 */
public class BusinessRuleException extends AppException {

    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(ErrorCode.BUSINESS_RULE, message);
    }
}
