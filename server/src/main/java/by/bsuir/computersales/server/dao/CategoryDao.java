
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о категориях товаров.
 */
public interface CategoryDao {

    /**
     * Получает список всех категорий.
     *
     * @return список категорий
     */
    List<Category> findAll();

    /**
     * Находит категорию по идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return найденная категория или пустой Optional
     */
    Optional<Category> findById(Long categoryId);
}
