
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о платежах.
 */
public interface PaymentDao {

    /**
     * Возвращает список всех платежей.
     *
     * @return список платежей
     */
    List<Payment> findAll();

    /**
     * Находит платёж по идентификатору.
     *
     * @param paymentId идентификатор платежа
     * @return найденный платёж или пустой Optional
     */
    Optional<Payment> findById(Long paymentId);

    /**
     * Возвращает платежи указанной продажи.
     *
     * @param saleId идентификатор продажи
     * @return список платежей
     */
    List<Payment> findBySaleId(Long saleId);

    /**
     * Возвращает платежи по заявке на рассрочку.
     *
     * @param applicationId идентификатор заявки
     * @return список платежей
     */
    List<Payment> findByApplicationId(Long applicationId);

    /**
     * Регистрирует новый платёж.
     *
     * @param payment данные платежа
     * @return идентификатор созданного платежа
     */
    Long create(Payment payment);
}
