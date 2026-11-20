
package by.bsuir.computersales.server.factory;

import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.Monitor;
import by.bsuir.computersales.common.model.Product;
import by.bsuir.computersales.common.model.ProductType;

import java.math.BigDecimal;

/**
 * Создатель объектов типа Monitor.
 */
public class MonitorCreator extends ProductCreator {

    @Override
    protected Product createProduct(
            Long productId,
            Category category,
            String manufacturer,
            String model,
            String productName,
            String specification,
            BigDecimal price,
            int stockQuantity,
            boolean active) {

        return new Monitor(
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

    @Override
    protected ProductType getSupportedType() {
        return ProductType.MONITOR;
    }
}
