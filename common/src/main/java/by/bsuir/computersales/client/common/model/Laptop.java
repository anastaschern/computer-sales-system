
package by.bsuir.computersales.client.common.model;

import java.math.BigDecimal;

/**
 * Класс ноутбука.
 */
public class Laptop extends Product {

    private static final long serialVersionUID = 1L;

    public Laptop(Long productId,
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
        return ProductType.LAPTOP;
    }

    @Override
    public String describe() {
        return formatDescription("Ноутбук");
    }
}
