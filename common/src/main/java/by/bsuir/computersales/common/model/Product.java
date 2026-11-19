
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Абстрактный базовый класс компьютерного товара.
 * Содержит общие характеристики всех товаров.
 */
public abstract class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Category category;
    private String manufacturer;
    private String model;
    private String productName;
    private String specification;
    private BigDecimal price;
    private int stockQuantity;
    private boolean active;

    public Product(Long productId,
                   Category category,
                   String manufacturer,
                   String model,
                   String productName,
                   String specification,
                   BigDecimal price,
                   int stockQuantity,
                   boolean active) {

        if (price == null) {
            throw new IllegalArgumentException("Цена товара не может быть null");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена товара не может быть отрицательной");
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Количество товара не может быть отрицательным");
        }

        this.productId = productId;
        this.category = category;
        this.manufacturer = manufacturer;
        this.model = model;
        this.productName = productName;
        this.specification = specification;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    // Получение типа товара
    public abstract ProductType getProductType();

    // Формирование краткого описания товара
    public abstract String describe();

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена должна быть неотрицательной");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Количество товара не может быть отрицательным");
        }
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return describe();
    }

    /**
     * Формирует общее текстовое описание товара.
     *
     * @param typeLabel название типа товара
     * @return краткое описание товара
     */

    protected String formatDescription(String typeLabel) {
        StringBuilder sb = new StringBuilder(typeLabel);

        // Добавляем производителя, если он указан
        if (manufacturer != null && !manufacturer.isBlank()) {
            sb.append(" ").append(manufacturer.trim());
        }

        // Добавляем модель, если она указана
        if (model != null && !model.isBlank()) {
            sb.append(" ").append(model.trim());
        }

        // Добавляем спецификацию, если она указана
        if (specification != null && !specification.isBlank()) {
            sb.append(" – ").append(specification.trim());
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Product product = (Product) object;

        return productId != null
                && productId.equals(product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), productId);
    }
}
