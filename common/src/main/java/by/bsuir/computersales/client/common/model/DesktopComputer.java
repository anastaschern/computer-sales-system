
package by.bsuir.computersales.client.common.model;

import java.math.BigDecimal;

/**
 * Класс настольного компьютера.
 */
public class DesktopComputer extends Product {

    private static final long serialVersionUID = 1L;

    public DesktopComputer(Long productId,
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
        return ProductType.DESKTOP_COMPUTER;
    }

    @Override
    public String describe() {
        return formatDescription("Настольный компьютер");
    }
}
