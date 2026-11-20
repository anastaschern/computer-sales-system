
package by.bsuir.computersales.client.common.protocol;

/**
 * Типы запросов, поддерживаемых сервером.
 */
public enum RequestType {

    // Авторизация и управление сессией
    LOGIN,
    LOGOUT,

    // Каталог товаров
    GET_PRODUCTS,
    GET_PRODUCT_BY_ID,
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DEACTIVATE_PRODUCT,

    // Категории товаров
    GET_CATEGORIES,
    CREATE_CATEGORY,
    UPDATE_CATEGORY,

    // Покупатели
    GET_CUSTOMERS,
    GET_CUSTOMER_BY_ID,
    CREATE_CUSTOMER,
    UPDATE_CUSTOMER,

    // Продажи
    GET_SALES,
    GET_SALE_BY_ID,
    CREATE_SALE,
    CANCEL_SALE,

    // Рассрочка
    CREATE_INSTALLMENT_APPLICATION,
    GET_INSTALLMENT_APPLICATIONS,
    GET_INSTALLMENT_APPLICATION_BY_ID,
    REVIEW_APPLICATION,
    GET_PAYMENT_SCHEDULE,

    // Платежи
    REGISTER_PAYMENT,
    GET_PAYMENTS,

    // Пользователи
    GET_USERS,
    GET_USER_BY_ID,
    CREATE_USER,
    UPDATE_USER,
    BLOCK_USER,

    // Отчёты и журнал
    GENERATE_REPORT,
    GET_AUDIT_LOG,

    // Завершение соединения
    DISCONNECT
}
