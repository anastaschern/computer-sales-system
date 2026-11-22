
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Customer;
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

class CustomerDaoImplTest {

    private static DatabaseManager databaseManager;

    private CustomerDao customerDao;

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
        customerDao = new CustomerDaoImpl(databaseManager);
    }

    @Test
    void findAllShouldReturnAllCustomers() {
        List<Customer> customers = customerDao.findAll();

        assertEquals(4, customers.size());
    }

    @Test
    void findAllShouldReturnCustomersWithRequiredData() {
        List<Customer> customers = customerDao.findAll();

        assertFalse(customers.isEmpty());

        for (Customer customer : customers) {
            assertNotNull(customer.getCustomerId());
            assertNotNull(customer.getFullName());
            assertFalse(customer.getFullName().isBlank());
        }
    }

    @Test
    void findByIdShouldReturnCustomerWhenCustomerExists() {
        List<Customer> customers = customerDao.findAll();

        Long customerId = customers.get(0).getCustomerId();

        Optional<Customer> result = customerDao.findById(customerId);

        assertTrue(result.isPresent());
        assertEquals(customerId, result.get().getCustomerId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenCustomerDoesNotExist() {
        Optional<Customer> result = customerDao.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerDao.findById(null)
        );
    }

    @Test
    void findByIdShouldRejectZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerDao.findById(0L)
        );
    }

    @Test
    void findByIdShouldRejectNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerDao.findById(-1L)
        );
    }

    @Test
    void findByIdShouldReturnCustomerWithOptionalFields() {
        List<Customer> customers = customerDao.findAll();

        Customer customer = customers.get(0);

        Optional<Customer> result =
                customerDao.findById(customer.getCustomerId());

        assertTrue(result.isPresent());
        assertEquals(customer.getFullName(), result.get().getFullName());
        assertEquals(customer.getPhone(), result.get().getPhone());
        assertEquals(customer.getEmail(), result.get().getEmail());
        assertEquals(
                customer.getDocumentReference(),
                result.get().getDocumentReference()
        );
    }
}

