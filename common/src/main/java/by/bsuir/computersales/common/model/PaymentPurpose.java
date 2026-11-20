
package by.bsuir.computersales.common.model;

/**
 * Назначение внесённого платежа.
 */
public enum PaymentPurpose {

    // Полная оплата стоимости продажи.
    FULL_PAYMENT,

    // Оплата первоначального взноса по рассрочке.
    DOWN_PAYMENT,

    // Очередной платёж по графику рассрочки.
    INSTALLMENT_PAYMENT
}
