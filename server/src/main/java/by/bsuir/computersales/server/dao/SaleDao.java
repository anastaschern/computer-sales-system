
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.common.model.SaleStatus;

import java.util.List;
import java.util.Optional;
import java.sql.Connection;

/**
 * Интерфейс доступа к данным о продажах.
 */
public interface SaleDao {

    /**
     * Возвращает список всех продаж.
     *
     * @return список продаж
     */
    List<Sale> findAll();

    /**
     * Находит продажу по идентификатору.
     *
     * @param saleId идентификатор продажи
     * @return найденная продажа или пустой Optional
     */
    Optional<Sale> findById(Long saleId);

    /**
     * Возвращает продажи указанного покупателя.
     *
     * @param customerId идентификатор покупателя
     * @return список продаж покупателя
     */
    List<Sale> findByCustomerId(Long customerId);

    /**
     * Возвращает продажи, оформленные указанным сотрудником.
     *
     * @param userId идентификатор сотрудника
     * @return список продаж сотрудника
     */
    List<Sale> findByUserId(Long userId);

    /**
     * Оформляет новую продажу в базе данных.
     *
     * Создание продажи, добавление товарных позиций
     * и уменьшение остатков должны выполняться
     * в рамках одной транзакции.
     *
     * @param userId идентификатор сотрудника
     * @param saleData данные оформляемой продажи
     * @return идентификатор созданной продажи
     */
    Long createSale(Long userId, CreateSaleData saleData);
    /**
     * Блокирует строку продажи до завершения текущей транзакции.
     *
     * @param connection соединение текущей транзакции
     * @param saleId идентификатор продажи
     */
    default void lockById(Connection connection, Long saleId) {
        throw new UnsupportedOperationException(
                "Блокировка продажи не реализована"
        );
    }
    /**
     * Изменяет статус продажи в рамках существующей транзакции.
     *
     * @param connection соединение текущей транзакции
     * @param saleId идентификатор продажи
     * @param status новый статус продажи
     */
    default void updateStatus(
            Connection connection,
            Long saleId,
            SaleStatus status
    ) {
        throw new UnsupportedOperationException(
                "Обновление статуса продажи не реализовано"
        );
    }
}
