
package by.bsuir.computersales.client.common.model;

import java.math.BigDecimal;

/**
 * Класс периферийного устройства.
 */
public class PeripheralDevice extends Product {

    private static final long serialVersionUID = 1L;

    public PeripheralDevice(Long productId,
                            Category category,
                            String manufacturer,
                            String model,
                            String productName,
                            String specification,
                            BigDecimal price,
                            int stockQuantity,
                            boolean active) {

        super(productId, category, manufacturer, model,
                productName, specification, price, stockQuantity, active);
    }

    @Override
    public ProductType getProductType() {
        return ProductType.PERIPHERAL_DEVICE;
    }

    @Override
    public String describe() {
        return formatDescription("Периферийное устройство");
    }
}
