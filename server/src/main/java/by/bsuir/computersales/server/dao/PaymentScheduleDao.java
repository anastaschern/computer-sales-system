
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.PaymentScheduleItem;

import java.util.List;

/**
 * Интерфейс доступа к данным графика платежей.
 */
public interface PaymentScheduleDao {

    /**
     * Возвращает график платежей указанной заявки.
     *
     * @param applicationId идентификатор заявки
     * @return список платежей графика
     */
    List<PaymentScheduleItem> findByApplicationId(Long applicationId);

    /**
     * Добавляет запись в график платежей.
     *
     * @param item запись графика
     */
    void create(PaymentScheduleItem item);

    /**
     * Добавляет несколько записей графика в рамках одной операции.
     *
     * @param items список записей графика
     */
    void createAll(List<PaymentScheduleItem> items);

    /**
     * Удаляет график платежей указанной заявки.
     *
     * @param applicationId идентификатор заявки
     */
    void deleteByApplicationId(Long applicationId);
}
