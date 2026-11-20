
package by.bsuir.computersales.client.common.model;

import java.math.BigDecimal;

/**
 * Класс монитора.
 */
public class Monitor extends Product {

    private static final long serialVersionUID = 1L;

    public Monitor(Long productId,
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
        return ProductType.MONITOR;
    }

    @Override
    public String describe() {
        return formatDescription("Монитор");
    }
}
