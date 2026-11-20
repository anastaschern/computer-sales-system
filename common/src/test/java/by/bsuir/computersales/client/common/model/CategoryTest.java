package by.bsuir.computersales.client.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateCategory() {
        Category category = new Category(
                1L,
                "Ноутбуки",
                "Портативные компьютеры",
                ProductType.LAPTOP
        );

        assertEquals(1L, category.getCategoryId());
        assertEquals("Ноутбуки", category.getCategoryName());
        assertEquals("Портативные компьютеры", category.getDescription());
        assertEquals(ProductType.LAPTOP, category.getProductType());
    }

    @Test
    void shouldRejectNullProductTypeInConstructor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Category(
                        1L,
                        "Ноутбуки",
                        "Портативные компьютеры",
                        null
                )
        );
    }

    @Test
    void shouldRejectNullProductTypeInSetter() {
        Category category = new Category(
                1L,
                "Ноутбуки",
                "Портативные компьютеры",
                ProductType.LAPTOP
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> category.setProductType(null)
        );
    }

    @Test
    void shouldReturnCategoryNameInToString() {
        Category category = new Category(
                1L,
                "Ноутбуки",
                "Портативные компьютеры",
                ProductType.LAPTOP
        );

        assertEquals("Ноутбуки", category.toString());
    }
    @Test
    void shouldConsiderCategoriesWithSameIdEqual() {
        Category first = new Category(
                1L, "Ноутбуки", "Описание 1", ProductType.LAPTOP
        );

        Category second = new Category(
                1L, "Другие ноутбуки", "Описание 2", ProductType.LAPTOP
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderCategoriesWithDifferentIdsNotEqual() {
        Category first = new Category(
                1L, "Ноутбуки", "Описание", ProductType.LAPTOP
        );

        Category second = new Category(
                2L, "Ноутбуки", "Описание", ProductType.LAPTOP
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotConsiderDifferentCategoriesWithoutIdEqual() {
        Category first = new Category(
                null, "Ноутбуки", "Описание", ProductType.LAPTOP
        );

        Category second = new Category(
                null, "Ноутбуки", "Описание", ProductType.LAPTOP
        );

        assertNotEquals(first, second);
    }
}