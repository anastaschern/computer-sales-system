
package by.bsuir.computersales.client.common.model;

import java.io.Serializable;
import java.util.Objects;


/**
 * Модель категории товаров.
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String categoryName;
    private String description;
    private ProductType productType;

    public Category() {
    }

    public Category(
            Long categoryId,
            String categoryName,
            String description,
            ProductType productType
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        setProductType(productType);
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        if (productType == null) {
            throw new IllegalArgumentException(
                    "Тип товара не может быть null"
            );
        }

        this.productType = productType;
    }

    @Override
    public String toString() {
        return categoryName;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Category category = (Category) object;

        return categoryId != null
                && categoryId.equals(category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }
}
