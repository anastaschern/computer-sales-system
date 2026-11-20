
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о товарах.
 */
public interface ProductDao {

    /**
     * Получает список всех товаров.
     *
     * @return список товаров
     */
    List<Product> findAll();

    /**
     * Находит товар по идентификатору.
     *
     * @param productId идентификатор товара
     * @return найденный товар или пустой Optional
     */
    Optional<Product> findById(Long productId);

    /**
     * Получает список активных товаров.
     *
     * @return список активных товаров
     */
    List<Product> findAllActive();
}
