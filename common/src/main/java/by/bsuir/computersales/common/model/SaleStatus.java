
package by.bsuir.computersales.common.model;

/**
 * Статус продажи.
 */
public enum SaleStatus {

    // Продажа оформлена, но ещё не оплачена.
    REGISTERED,

    // Продажа оплачена.
    PAID,

    // Продажа полностью завершена.
    COMPLETED
}
