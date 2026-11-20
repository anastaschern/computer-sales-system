
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.DesktopComputer;
import by.bsuir.computersales.client.common.model.Laptop;
import by.bsuir.computersales.client.common.model.Monitor;
import by.bsuir.computersales.client.common.model.PeripheralDevice;
import by.bsuir.computersales.client.common.model.Product;
import by.bsuir.computersales.client.server.config.ServerConfig;
import by.bsuir.computersales.client.server.database.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoImplTest {

    private static DatabaseManager databaseManager;

    private ProductDao productDao;

    @BeforeAll
    static void setUpDatabase() {
        ServerConfig config = ServerConfig.load(Path.of("../server.properties"));
        databaseManager = new DatabaseManager(config);
    }

    @AfterAll
    static void closeDatabase() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @BeforeEach
    void setUp() {
        productDao = new ProductDaoImpl(databaseManager);
    }

    @Test
    void findAllShouldReturnAllProducts() {
        List<Product> products = productDao.findAll();

        assertEquals(9, products.size());
    }

    @Test
    void findAllShouldReturnAllProductTypes() {
        List<Product> products = productDao.findAll();

        assertTrue(products.stream().anyMatch(
                Laptop.class::isInstance
        ));

        assertTrue(products.stream().anyMatch(
                DesktopComputer.class::isInstance
        ));

        assertTrue(products.stream().anyMatch(
                Monitor.class::isInstance
        ));

        assertTrue(products.stream().anyMatch(
                PeripheralDevice.class::isInstance
        ));
    }

    @Test
    void findAllShouldReturnProductsWithRequiredData() {
        List<Product> products = productDao.findAll();

        assertFalse(products.isEmpty());

        for (Product product : products) {
            assertNotNull(product.getProductId());
            assertNotNull(product.getCategory());
            assertNotNull(product.getProductName());
            assertNotNull(product.getProductType());
            assertNotNull(product.getPrice());
            assertTrue(product.getPrice().signum() >= 0);
            assertTrue(product.getStockQuantity() >= 0);
        }
    }

    @Test
    void findByIdShouldReturnProductWhenProductExists() {
        List<Product> products = productDao.findAll();

        Long productId = products.get(0).getProductId();

        Optional<Product> result = productDao.findById(productId);

        assertTrue(result.isPresent());
        assertEquals(productId, result.get().getProductId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenProductDoesNotExist() {
        Optional<Product> result = productDao.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productDao.findById(null)
        );
    }

    @Test
    void findByIdShouldRejectZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productDao.findById(0L)
        );
    }

    @Test
    void findByIdShouldRejectNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productDao.findById(-1L)
        );
    }

    @Test
    void findByIdShouldReturnProductWithCorrectCategory() {
        List<Product> products = productDao.findAll();

        Product expected = products.get(0);

        Optional<Product> result =
                productDao.findById(expected.getProductId());

        assertTrue(result.isPresent());

        Product actual = result.get();

        assertEquals(
                expected.getCategory().getCategoryId(),
                actual.getCategory().getCategoryId()
        );

        assertEquals(
                expected.getProductType(),
                actual.getProductType()
        );
    }

    @Test
    void findAllActiveShouldReturnOnlyActiveProducts() {
        List<Product> activeProducts = productDao.findAllActive();

        assertTrue(activeProducts.stream().allMatch(Product::isActive));
    }

    @Test
    void findAllActiveShouldNotContainInactiveProducts() {
        List<Product> allProducts = productDao.findAll();
        List<Product> activeProducts = productDao.findAllActive();

        long expectedCount = allProducts.stream()
                .filter(Product::isActive)
                .count();

        assertEquals(expectedCount, activeProducts.size());
    }
}
