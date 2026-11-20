package by.bsuir.computersales.common.model;

/**
 * Статусы заявки на оформление рассрочки.
 */
public enum InstallmentApplicationStatus {

    /** Заявка создана. */
    CREATED,

    /** Заявка находится на рассмотрении. */
    ON_REVIEW,

    /** Заявка одобрена. */
    APPROVED,

    /** В оформлении рассрочки отказано. */
    REJECTED,

    /** Заявка возвращена на уточнение. */
    RETURNED,

    /** Рассрочка выполняется. */
    IN_PROGRESS,

    /** Рассрочка полностью завершена. */
    COMPLETED
}