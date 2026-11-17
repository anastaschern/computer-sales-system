
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Абстрактный родительский класс для всех видов товаров.
 * Содержит общие характеристики компьютерной техники.
 */
public abstract class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    // Уникальный идентификатор товара
    private Long productId;

    // Категория товара
    private Category category;

    // Производитель
    private String manufacturer;

    // Модель товара
    private String model;

    // Наименование товара
    private String productName;

    // Технические характеристики
    private String specification;

    // Цена товара
    private BigDecimal price;

    // Количество товара на складе
    private int stockQuantity;

    // Активен ли товар
    private boolean active;

    /**
     * Конструктор родительского класса.
     */
    public Product(Long productId,
                   Category category,
                   String manufacturer,
                   String model,
                   String productName,
                   String specification,
                   BigDecimal price,
                   int stockQuantity,
                   boolean active) {

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

    /**
     * Возвращает тип товара.
     * Реализация будет определяться дочерними классами.
     */
    public abstract ProductType getProductType();

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
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
