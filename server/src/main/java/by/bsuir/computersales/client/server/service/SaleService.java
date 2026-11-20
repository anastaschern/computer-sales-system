
package by.bsuir.computersales.client.server.service;

import by.bsuir.computersales.client.common.dto.CreateSaleData;
import by.bsuir.computersales.client.common.model.Sale;
import by.bsuir.computersales.client.server.dao.SaleDao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Сервис для выполнения операций с продажами.
 *
 * Содержит бизнес-логику работы с продажами
 * и обращается к базе данных через SaleDao.
 */
public class SaleService {

    private final SaleDao saleDao;

    /**
     * Создаёт сервис продаж.
     *
     * @param saleDao объект доступа к данным продаж
     */
    public SaleService(SaleDao saleDao) {
        this.saleDao = Objects.requireNonNull(
                saleDao,
                "SaleDao не должен быть null"
        );
    }

    /**
     * Получает список всех продаж.
     *
     * @return список продаж
     */
    public List<Sale> findAll() {
        return saleDao.findAll();
    }

    /**
     * Находит продажу по идентификатору.
     *
     * @param saleId идентификатор продажи
     * @return найденная продажа или пустой Optional
     */
    public Optional<Sale> findById(Long saleId) {
        return saleDao.findById(saleId);
    }

    /**
     * Получает продажи указанного покупателя.
     *
     * @param customerId идентификатор покупателя
     * @return список продаж покупателя
     */
    public List<Sale> findByCustomerId(Long customerId) {
        return saleDao.findByCustomerId(customerId);
    }

    /**
     * Получает продажи указанного сотрудника.
     *
     * @param userId идентификатор сотрудника
     * @return список продаж сотрудника
     */
    public List<Sale> findByUserId(Long userId) {
        return saleDao.findByUserId(userId);
    }

    /**
     * Оформляет новую продажу.
     *
     * @param userId идентификатор сотрудника,
     *               выполняющего операцию
     * @param saleData данные новой продажи
     * @return идентификатор созданной продажи
     */
    public Long createSale(Long userId, CreateSaleData saleData) {
        return saleDao.createSale(userId, saleData);
    }
}
