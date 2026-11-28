package by.bsuir.computersales.common.model;

/**
 * Статусы продажи.
 */
public enum SaleStatus {
    CREATED,
    REGISTERED,  // <-- если используется в БД
    PAID,
    COMPLETED,
    CANCELLED
}