
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Sale;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о продажах.
 */
public interface SaleDao {

    /**
     * Возвращает список всех продаж.
     */
    List<Sale> findAll();

    /**
     * Находит продажу по идентификатору.
     */
    Optional<Sale> findById(Long saleId);

    /**
     * Возвращает продажи указанного покупателя.
     */
    List<Sale> findByCustomerId(Long customerId);

    /**
     * Возвращает продажи, оформленные указанным сотрудником.
     */
    List<Sale> findByUserId(Long userId);
}
