
package by.bsuir.computersales.common.model;

import java.math.BigDecimal;

/**
 * Класс, описывающий настольный компьютер.
 * Наследуется от абстрактного класса Product.
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
                productName, specification, price,
                stockQuantity, active);
    }

    /**
     * Возвращает тип товара.
     */
    @Override
    public ProductType getProductType() {
        return ProductType.DESKTOP_COMPUTER;
    }
}
