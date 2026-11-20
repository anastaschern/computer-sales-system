
package by.bsuir.computersales.server;

import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.Product;
import by.bsuir.computersales.common.model.ProductType;
import by.bsuir.computersales.server.exception.ValidationException;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Фабрика для выбора подходящего создателя компьютерного товара.
 */
public class ProductFactory {

    private final Map<ProductType, ProductCreator> creators;

    /**
     * Регистрирует создателей всех поддерживаемых типов товаров.
     */
    public ProductFactory() {
        creators = new EnumMap<>(ProductType.class);

        registerCreator(new LaptopCreator());
        registerCreator(new DesktopComputerCreator());
        registerCreator(new MonitorCreator());
        registerCreator(new PeripheralDeviceCreator());
    }

    /**
     * Регистрирует создателя по поддерживаемому им типу.
     */
    private void registerCreator(ProductCreator creator) {
        creators.put(creator.getSupportedType(), creator);
    }

    /**
     * Создаёт товар в соответствии с типом его категории.
     */
    public Product createProduct(
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
                    "Категория товара не может быть null"
            );
        }

        ProductType type = category.getProductType();

        if (type == null) {
            throw new ValidationException(
                    "Тип товара в категории не задан"
            );
        }

        ProductCreator creator = creators.get(type);

        if (creator == null) {
            throw new ValidationException(
                    "Неизвестный тип товара: " + type
            );
        }

        return creator.create(
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
    }
}
