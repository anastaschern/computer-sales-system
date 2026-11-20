package by.bsuir.computersales.common.model;

import by.bsuir.computersales.common.model.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomer() {
        Customer customer = new Customer(
                1L,
                "Иванов Иван Иванович",
                "+375291234567",
                "ivan@example.com",
                "AB1234567"
        );

        assertEquals(1L, customer.getCustomerId());
        assertEquals("Иванов Иван Иванович", customer.getFullName());
        assertEquals("+375291234567", customer.getPhone());
        assertEquals("ivan@example.com", customer.getEmail());
        assertEquals("AB1234567", customer.getDocumentReference());
    }

    @Test
    void shouldConsiderCustomersWithSameIdEqual() {
        Customer first = new Customer(
                1L, "Иванов Иван", "+375291111111",
                "first@example.com", "AB1111111"
        );

        Customer second = new Customer(
                1L, "Петров Петр", "+375292222222",
                "second@example.com", "AB2222222"
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderCustomersWithDifferentIdsNotEqual() {
        Customer first = new Customer(
                1L, "Иванов Иван", null, null, null
        );

        Customer second = new Customer(
                2L, "Иванов Иван", null, null, null
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotConsiderDifferentCustomersWithoutIdEqual() {
        Customer first = new Customer(
                null, "Иванов Иван", null, null, null
        );

        Customer second = new Customer(
                null, "Иванов Иван", null, null, null
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnFullNameInToString() {
        Customer customer = new Customer(
                1L, "Иванов Иван Иванович", null, null, null
        );

        assertEquals("Иванов Иван Иванович", customer.toString());
    }
}