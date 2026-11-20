
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Товарная позиция в составе продажи.
 */
public class SaleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private int lineNumber;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;

    public SaleItem() {
    }

    public SaleItem(
            int lineNumber,
            Product product,
            int quantity,
            BigDecimal unitPrice) {

        setLineNumber(lineNumber);
        setProduct(product);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        if (lineNumber <= 0) {
            throw new IllegalArgumentException(
                    "Номер позиции должен быть положительным"
            );
        }

        this.lineNumber = lineNumber;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = Objects.requireNonNull(
                product,
                "Товар не должен быть null"
        );
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество товара должно быть положительным"
            );
        }

        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Цена товара не может быть null или отрицательной"
            );
        }

        this.unitPrice = unitPrice;
    }

    /**
     * Возвращает стоимость товарной позиции.
     */
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
