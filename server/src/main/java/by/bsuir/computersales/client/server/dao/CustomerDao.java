package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о покупателях.
 */
public interface CustomerDao {

    /**
     * Получает список всех покупателей.
     *
     * @return список покупателей
     */
    List<Customer> findAll();

    /**
     * Находит покупателя по идентификатору.
     *
     * @param customerId идентификатор покупателя
     * @return найденный покупатель или пустой Optional
     */
    Optional<Customer> findById(Long customerId);
}