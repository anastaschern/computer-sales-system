
package by.bsuir.computersales.client.server.factory;

import by.bsuir.computersales.client.common.model.Category;
import by.bsuir.computersales.client.common.model.PeripheralDevice;
import by.bsuir.computersales.client.common.model.Product;
import by.bsuir.computersales.client.common.model.ProductType;

import java.math.BigDecimal;

/**
 * Создатель объектов типа PeripheralDevice.
 */
public class PeripheralDeviceCreator extends ProductCreator {

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

        return new PeripheralDevice(
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
        return ProductType.PERIPHERAL_DEVICE;
    }
}

