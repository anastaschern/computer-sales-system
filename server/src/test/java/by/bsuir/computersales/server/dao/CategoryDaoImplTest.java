
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.ProductType;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.database.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDaoImplTest {

    private static DatabaseManager databaseManager;

    private CategoryDao categoryDao;

    @BeforeAll
    static void setUpDatabase() {
        ServerConfig config = ServerConfig.load(Path.of("../server-test.properties"));
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
        categoryDao = new CategoryDaoImpl(databaseManager);
    }

    @Test
    void findAllShouldReturnAllCategories() {
        List<Category> categories = categoryDao.findAll();

        assertEquals(4, categories.size());
    }

    @Test
    void findAllShouldReturnCategoriesWithCorrectProductTypes() {
        List<Category> categories = categoryDao.findAll();

        assertTrue(categories.stream().anyMatch(category ->
                category.getProductType() == ProductType.LAPTOP));

        assertTrue(categories.stream().anyMatch(category ->
                category.getProductType() == ProductType.DESKTOP_COMPUTER));

        assertTrue(categories.stream().anyMatch(category ->
                category.getProductType() == ProductType.MONITOR));

        assertTrue(categories.stream().anyMatch(category ->
                category.getProductType() == ProductType.PERIPHERAL_DEVICE));
    }

    @Test
    void findAllShouldReturnCategoriesWithRequiredData() {
        List<Category> categories = categoryDao.findAll();

        assertFalse(categories.isEmpty());

        for (Category category : categories) {
            assertNotNull(category.getCategoryId());
            assertNotNull(category.getCategoryName());
            assertFalse(category.getCategoryName().isBlank());
            assertNotNull(category.getProductType());
        }
    }

    @Test
    void findByIdShouldReturnCategoryWhenCategoryExists() {
        List<Category> categories = categoryDao.findAll();

        Long categoryId = categories.get(0).getCategoryId();

        Optional<Category> result = categoryDao.findById(categoryId);

        assertTrue(result.isPresent());
        assertEquals(categoryId, result.get().getCategoryId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenCategoryDoesNotExist() {
        Optional<Category> result = categoryDao.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> categoryDao.findById(null)
        );
    }

    @Test
    void findByIdShouldRejectZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> categoryDao.findById(0L)
        );
    }

    @Test
    void findByIdShouldRejectNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> categoryDao.findById(-1L)
        );
    }

    @Test
    void findByIdShouldReturnAllCategoryFields() {
        List<Category> categories = categoryDao.findAll();

        Category expected = categories.get(0);

        Optional<Category> result =
                categoryDao.findById(expected.getCategoryId());

        assertTrue(result.isPresent());

        Category actual = result.get();

        assertEquals(expected.getCategoryName(), actual.getCategoryName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getProductType(), actual.getProductType());
    }
}


