
package by.bsuir.computersales.server.factory;

import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.Product;
import by.bsuir.computersales.common.model.ProductType;
import by.bsuir.computersales.server.exception.ValidationException;

import java.math.BigDecimal;

/**
 * Абстрактный создатель компьютерных товаров.
 *
 * Определяет общий алгоритм создания товара
 * и фабричный метод для создания конкретного объекта.
 */
public abstract class ProductCreator {

    /**
     * Создаёт товар после проверки входных данных.
     */
    public final Product create(
            Long productId,
            Category category,
            String manufacturer,
            String model,
            String productName,
            String specification,
            BigDecimal price,
            int stockQuantity,
            boolean active) {

        if (category == null) {
            throw new ValidationException(
                    "Категория товара не указана"
            );
        }

        if (category.getProductType() == null) {
            throw new ValidationException(
                    "У категории не задан тип товара"
            );
        }

        if (category.getProductType() != getSupportedType()) {
            throw new ValidationException(
                    "Тип товара не соответствует выбранной фабрике"
            );
        }

        try {
            return createProduct(
                    productId,
                    category,
                    manufacturer,
                    model,
                    productName,
                    specification,
                    price,
                    stockQuantity,
                    active
            );
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }

    /**
     * Фабричный метод создания конкретного товара.
     */
    protected abstract Product createProduct(
            Long productId,
            Category category,
            String manufacturer,
            String model,
            String productName,
            String specification,
            BigDecimal price,
            int stockQuantity,
            boolean active
    );

    /**
     * Возвращает тип товара, поддерживаемый создателем.
     */
    protected abstract ProductType getSupportedType();
}
