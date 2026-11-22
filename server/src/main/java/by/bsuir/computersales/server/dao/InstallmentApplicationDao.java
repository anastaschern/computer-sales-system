
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.InstallmentApplicationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о заявках на рассрочку.
 */
public interface InstallmentApplicationDao {

    /**
     * Возвращает список всех заявок на рассрочку.
     *
     * @return список заявок
     */
    List<InstallmentApplication> findAll();

    /**
     * Находит заявку по идентификатору.
     *
     * @param applicationId идентификатор заявки
     * @return найденная заявка или пустой Optional
     */
    Optional<InstallmentApplication> findById(Long applicationId);

    /**
     * Находит заявку по идентификатору продажи.
     *
     * @param saleId идентификатор продажи
     * @return найденная заявка или пустой Optional
     */
    Optional<InstallmentApplication> findBySaleId(Long saleId);

    /**
     * Возвращает заявки с указанным статусом.
     *
     * @param status статус заявки
     * @return список заявок
     */
    List<InstallmentApplication> findByStatus(
            InstallmentApplicationStatus status
    );

    /**
     * Возвращает заявки, рассмотренные указанным сотрудником.
     *
     * @param reviewerId идентификатор сотрудника
     * @return список заявок
     */
    List<InstallmentApplication> findByReviewerId(Long reviewerId);

    /**
     * Создаёт новую заявку на рассрочку.
     *
     * @param application данные заявки
     * @return идентификатор созданной заявки
     */
    Long create(InstallmentApplication application);

    /**
     * Обновляет существующую заявку.
     *
     * @param application изменённые данные заявки
     */
    void update(InstallmentApplication application);
}
