
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.dto.CreateSaleData;
import by.bsuir.computersales.client.common.model.Sale;

import java.util.List;
import java.util.Optional;

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
}
