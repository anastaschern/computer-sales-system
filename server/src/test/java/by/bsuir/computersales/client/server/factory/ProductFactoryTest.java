
package by.bsuir.computersales.client.server.factory;

import by.bsuir.computersales.client.common.model.Category;
import by.bsuir.computersales.client.common.model.DesktopComputer;
import by.bsuir.computersales.client.common.model.Laptop;
import by.bsuir.computersales.client.common.model.Monitor;
import by.bsuir.computersales.client.common.model.PeripheralDevice;
import by.bsuir.computersales.client.common.model.Product;
import by.bsuir.computersales.client.common.model.ProductType;
import by.bsuir.computersales.client.server.exception.ValidationException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование фабрики компьютерных товаров.
 */
class ProductFactoryTest {

    private final ProductFactory factory = new ProductFactory();

    private Category createCategory(ProductType type) {
        return new Category(
                1L,
                "Тестовая категория",
                "Категория для проверки фабрики",
                type
        );
    }

    private Product createProduct(ProductType type) {
        return factory.createProduct(
                1L,
                createCategory(type),
                "Test",
                "Model",
                "Тестовый товар",
                "Характеристики",
                new BigDecimal("1000.00"),
                10,
                true
        );
    }

    @Test
    void shouldCreateLaptop() {
        Product product = createProduct(ProductType.LAPTOP);

        assertInstanceOf(Laptop.class, product);
        assertEquals(ProductType.LAPTOP, product.getProductType());
    }

    @Test
    void shouldCreateDesktopComputer() {
        Product product = createProduct(ProductType.DESKTOP_COMPUTER);

        assertInstanceOf(DesktopComputer.class, product);
        assertEquals(ProductType.DESKTOP_COMPUTER, product.getProductType());
    }

    @Test
    void shouldCreateMonitor() {
        Product product = createProduct(ProductType.MONITOR);

        assertInstanceOf(Monitor.class, product);
        assertEquals(ProductType.MONITOR, product.getProductType());
    }

    @Test
    void shouldCreatePeripheralDevice() {
        Product product = createProduct(ProductType.PERIPHERAL_DEVICE);

        assertInstanceOf(PeripheralDevice.class, product);
        assertEquals(ProductType.PERIPHERAL_DEVICE, product.getProductType());
    }

    /**
     * Проверяет наличие создателя для каждого значения ProductType.
     */
    @Test
    void shouldCreateProductForEveryProductType() {
        for (ProductType type : ProductType.values()) {
            Product product = createProduct(type);

            assertNotNull(product);
            assertEquals(type, product.getProductType());
        }
    }

    /**
     * Проверяет отклонение категории без указанного типа.
     */
    @Test
    void shouldRejectCategoryWithoutProductType() {
        Category category = new Category();

        assertThrows(
                ValidationException.class,
                () -> factory.createProduct(
                        5L,
                        category,
                        "Test",
                        "Model",
                        "Тестовый товар",
                        "Характеристики",
                        new BigDecimal("100.00"),
                        5,
                        true
                )
        );
    }

    /**
     * Проверяет отклонение отсутствующей категории.
     */
    @Test
    void shouldRejectNullCategory() {
        assertThrows(
                ValidationException.class,
                () -> factory.createProduct(
                        5L,
                        null,
                        "Test",
                        "Model",
                        "Тестовый товар",
                        "Характеристики",
                        new BigDecimal("100.00"),
                        5,
                        true
                )
        );
    }

    /**
     * Проверяет несоответствие типа категории и создателя.
     */
    @Test
    void shouldRejectMismatchedCreatorAndCategory() {
        LaptopCreator laptopCreator = new LaptopCreator();

        assertThrows(
                ValidationException.class,
                () -> laptopCreator.create(
                        6L,
                        createCategory(ProductType.MONITOR),
                        "Samsung",
                        "Odyssey",
                        "Монитор",
                        "27 дюймов",
                        new BigDecimal("1000.00"),
                        5,
                        true
                )
        );
    }


    @Test
    void shouldRejectNegativePrice() {
        assertThrows(
                ValidationException.class,
                () -> factory.createProduct(
                        7L,
                        createCategory(ProductType.LAPTOP),
                        "Lenovo",
                        "ThinkPad",
                        "Ноутбук",
                        "16 GB RAM",
                        new BigDecimal("-100.00"),
                        5,
                        true
                )
        );
    }


    @Test
    void shouldRejectNegativeStockQuantity() {
        assertThrows(
                ValidationException.class,
                () -> factory.createProduct(
                        8L,
                        createCategory(ProductType.LAPTOP),
                        "Lenovo",
                        "ThinkPad",
                        "Ноутбук",
                        "16 GB RAM",
                        new BigDecimal("1000.00"),
                        -5,
                        true
                )
        );
    }

}
