
package by.bsuir.computersales.client.common.model;

import by.bsuir.computersales.common.model.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    // Вспомогательная категория для тестирования товаров
    private Category createCategory() {
        return new Category(
                1L,
                "Ноутбуки",
                "Портативные компьютеры",
                ProductType.LAPTOP
        );
    }

    // Проверка формирования описания ноутбука
    @Test
    void laptopShouldDescribeCorrectly() {
        Laptop laptop = new Laptop(
                1L, createCategory(), "Lenovo", "ThinkPad E14",
                "Ноутбук Lenovo ThinkPad E14", "16 GB RAM, 512 GB SSD",
                new BigDecimal("2500.00"), 10, true
        );

        assertEquals(
                "Ноутбук Lenovo ThinkPad E14 – 16 GB RAM, 512 GB SSD",
                laptop.describe()
        );
    }

    // Проверка формирования описания настольного компьютера
    @Test
    void desktopComputerShouldDescribeCorrectly() {
        DesktopComputer computer = new DesktopComputer(
                2L, createCategory(), "HP", "ProDesk 400",
                "Настольный компьютер HP", "Intel Core i5, 16 GB RAM",
                new BigDecimal("1800.00"), 5, true
        );

        assertEquals(
                "Настольный компьютер HP ProDesk 400 – Intel Core i5, 16 GB RAM",
                computer.describe()
        );
    }

    // Проверка формирования описания монитора
    @Test
    void monitorShouldDescribeCorrectly() {
        Monitor monitor = new Monitor(
                3L, createCategory(), "Samsung", "S24C",
                "Монитор Samsung", "24 дюйма, Full HD",
                new BigDecimal("650.00"), 7, true
        );

        assertEquals(
                "Монитор Samsung S24C – 24 дюйма, Full HD",
                monitor.describe()
        );
    }

    // Проверка формирования описания периферийного устройства
    @Test
    void peripheralDeviceShouldDescribeCorrectly() {
        PeripheralDevice device = new PeripheralDevice(
                4L, createCategory(), "Logitech", "M185",
                "Мышь Logitech", "Беспроводная мышь",
                new BigDecimal("50.00"), 20, true
        );

        assertEquals(
                "Периферийное устройство Logitech M185 – Беспроводная мышь",
                device.describe()
        );
    }

    // Проверка описания товара без спецификации
    @Test
    void laptopShouldDescribeWithoutSpecification() {
        Laptop laptop = new Laptop(
                5L, createCategory(), "Lenovo", "IdeaPad",
                "Ноутбук Lenovo", null,
                new BigDecimal("1500.00"), 3, true
        );

        assertEquals("Ноутбук Lenovo IdeaPad", laptop.describe());
    }

    // Проверка обработки пустых производителя и модели
    @Test
    void laptopShouldDescribeWithoutManufacturerAndModel() {
        Laptop laptop = new Laptop(
                6L, createCategory(), null, null,
                "Ноутбук", null,
                new BigDecimal("1500.00"), 3, true
        );

        assertEquals("Ноутбук", laptop.describe());
    }

    // Проверка отклонения отрицательной цены
    @Test
    void shouldRejectNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Laptop(
                        7L, createCategory(), "Lenovo", "ThinkPad",
                        "Ноутбук", "16 GB RAM",
                        new BigDecimal("-100.00"), 5, true
                )
        );
    }

    // Проверка отклонения отрицательного количества
    @Test
    void shouldRejectNegativeStockQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Laptop(
                        8L, createCategory(), "Lenovo", "ThinkPad",
                        "Ноутбук", "16 GB RAM",
                        new BigDecimal("1000.00"), -5, true
                )
        );
    }

    // Проверка равенства товаров с одинаковыми идентификаторами
    @Test
    void productsWithSameIdShouldBeEqual() {
        Laptop first = new Laptop(
                10L, createCategory(), "Lenovo", "ThinkPad",
                "Ноутбук", "16 GB RAM",
                new BigDecimal("1000.00"), 5, true
        );

        Laptop second = new Laptop(
                10L, createCategory(), "HP", "ProBook",
                "Ноутбук", "8 GB RAM",
                new BigDecimal("1200.00"), 3, true
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    // Проверка неравенства товаров с разными идентификаторами
    @Test
    void productsWithDifferentIdsShouldNotBeEqual() {
        Laptop first = new Laptop(
                10L, createCategory(), "Lenovo", "ThinkPad",
                "Ноутбук", "16 GB RAM",
                new BigDecimal("1000.00"), 5, true
        );

        Laptop second = new Laptop(
                11L, createCategory(), "Lenovo", "ThinkPad",
                "Ноутбук", "16 GB RAM",
                new BigDecimal("1000.00"), 5, true
        );

        assertNotEquals(first, second);
    }

    // Проверка сериализации ноутбука вместе с категорией
    @Test
    void laptopShouldBeSerializedAndDeserialized() throws Exception {
        Laptop original = new Laptop(
                15L, createCategory(), "Lenovo", "ThinkPad E14",
                "Ноутбук Lenovo ThinkPad E14", "16 GB RAM, 512 GB SSD",
                new BigDecimal("2500.00"), 10, true
        );

        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(original);
        }

        Laptop restored;

        try (ObjectInputStream inputStream = new ObjectInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            restored = (Laptop) inputStream.readObject();
        }

        assertEquals(original.getProductId(), restored.getProductId());
        assertEquals(original.getManufacturer(), restored.getManufacturer());
        assertEquals(original.getModel(), restored.getModel());
        assertEquals(original.getPrice(), restored.getPrice());

        assertNotNull(restored.getCategory());

        assertEquals(
                original.getCategory().getCategoryName(),
                restored.getCategory().getCategoryName()
        );

        assertEquals(original.describe(), restored.describe());
    }
}
