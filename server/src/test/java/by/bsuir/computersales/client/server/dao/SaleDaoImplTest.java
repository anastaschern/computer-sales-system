
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.Sale;
import by.bsuir.computersales.client.common.model.SaleItem;
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

class SaleDaoImplTest {

    private static DatabaseManager databaseManager;

    private SaleDao saleDao;

    @BeforeAll
    static void setUpDatabase() {
        ServerConfig config =
                ServerConfig.load(Path.of("../server.properties"));

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
        saleDao = new SaleDaoImpl(databaseManager);
    }

    @Test
    void findAllShouldReturnAllSales() {
        List<Sale> sales = saleDao.findAll();

        assertEquals(4, sales.size());
    }

    @Test
    void findAllShouldReturnSalesWithRequiredData() {
        List<Sale> sales = saleDao.findAll();

        assertFalse(sales.isEmpty());

        for (Sale sale : sales) {
            assertNotNull(sale.getSaleId());
            assertNotNull(sale.getCustomer());
            assertNotNull(sale.getCustomer().getCustomerId());
            assertNotNull(sale.getUser());
            assertNotNull(sale.getUser().getUserId());
            assertNotNull(sale.getSaleDate());
            assertNotNull(sale.getStatus());
            assertNotNull(sale.getPaymentType());
            assertNotNull(sale.getItems());
        }
    }

    @Test
    void findAllShouldReturnAllSaleItems() {
        List<Sale> sales = saleDao.findAll();

        int itemCount = sales.stream()
                .mapToInt(sale -> sale.getItems().size())
                .sum();

        assertEquals(5, itemCount);
    }

    @Test
    void findAllShouldReturnSaleItemsWithRequiredData() {
        List<Sale> sales = saleDao.findAll();

        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                assertTrue(item.getLineNumber() > 0);
                assertNotNull(item.getProduct());
                assertNotNull(item.getProduct().getProductId());
                assertTrue(item.getQuantity() > 0);
                assertNotNull(item.getUnitPrice());
                assertTrue(item.getUnitPrice().signum() >= 0);
            }
        }
    }

    @Test
    void findAllShouldCalculateTotalAmountCorrectly() {
        List<Sale> sales = saleDao.findAll();

        for (Sale sale : sales) {
            assertNotNull(sale.getTotalAmount());

            assertEquals(
                    sale.getItems().stream()
                            .map(SaleItem::getTotalPrice)
                            .reduce(
                                    java.math.BigDecimal.ZERO,
                                    java.math.BigDecimal::add
                            ),
                    sale.getTotalAmount()
            );
        }
    }

    @Test
    void findByIdShouldReturnSaleWhenSaleExists() {
        List<Sale> sales = saleDao.findAll();

        Long saleId = sales.get(0).getSaleId();

        Optional<Sale> result = saleDao.findById(saleId);

        assertTrue(result.isPresent());
        assertEquals(saleId, result.get().getSaleId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenSaleDoesNotExist() {
        Optional<Sale> result = saleDao.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findById(null)
        );
    }

    @Test
    void findByIdShouldRejectZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findById(0L)
        );
    }

    @Test
    void findByIdShouldRejectNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findById(-1L)
        );
    }

    @Test
    void findByCustomerIdShouldReturnOnlyCustomerSales() {
        List<Sale> allSales = saleDao.findAll();

        Long customerId = allSales.get(0)
                .getCustomer()
                .getCustomerId();

        List<Sale> customerSales =
                saleDao.findByCustomerId(customerId);

        assertFalse(customerSales.isEmpty());

        assertTrue(customerSales.stream().allMatch(
                sale -> sale.getCustomer()
                        .getCustomerId()
                        .equals(customerId)
        ));
    }

    @Test
    void findByCustomerIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByCustomerId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByCustomerId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByCustomerId(-1L)
        );
    }

    @Test
    void findByUserIdShouldReturnOnlyUserSales() {
        List<Sale> allSales = saleDao.findAll();

        Long userId = allSales.get(0)
                .getUser()
                .getUserId();

        List<Sale> userSales = saleDao.findByUserId(userId);

        assertFalse(userSales.isEmpty());

        assertTrue(userSales.stream().allMatch(
                sale -> sale.getUser()
                        .getUserId()
                        .equals(userId)
        ));
    }

    @Test
    void findByUserIdShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByUserId(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByUserId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleDao.findByUserId(-1L)
        );
    }
}
