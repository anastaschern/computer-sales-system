-- Тестовые данные для DAO-интеграционных тестов.
-- Выполнять только в installment_shop_test.
-- Предназначен для однократного выполнения после schema и seed.

BEGIN;

-- 1. Покупатели
INSERT INTO customers (
    customer_id, full_name, phone, email, document_reference
) VALUES
    (1, 'Иванов Иван Иванович', '+375291110001', 'ivanov@example.test', 'DOC-TEST-001'),
    (2, 'Петров Пётр Петрович', '+375291110002', 'petrov@example.test', 'DOC-TEST-002'),
    (3, 'Сидорова Анна Сергеевна', '+375291110003', 'sidorova@example.test', 'DOC-TEST-003'),
    (4, 'Козлов Алексей Викторович', '+375291110004', NULL, NULL);

-- 2. Пользователи
INSERT INTO users (
    user_id, login, password_hash, role_id, is_active
) VALUES
    (1, 'admin_test',
     'pbkdf2-sha256$600000$JNJKsroZw46e7zqnGAXJ5Q==$NwW6C/9jZhyIXdMRo0l1BlOsbpfq3YKJ0yncsp2qV6g=',
     1, TRUE),
    (2, 'manager_test',
     'pbkdf2-sha256$600000$9uRKwBz+EV8K8D8Ix4xBqA==$WYyqBJhU83gFZN5MoyrMQhQ5lEzn+NyeaQcLI9nJx0o=',
     2, TRUE),
    (3, 'specialist_test',
     'pbkdf2-sha256$600000$tXSnL53IsG71ylVETBL27w==$B7QDO5muBFYDhlQhDYCxVkkwqhnPiIfM0CmlzQ9n9go=',
     3, TRUE);

-- 3. Товары
-- Категории: 1 — ноутбуки, 2 — настольные компьютеры,
-- 3 — мониторы, 4 — периферийные устройства.
INSERT INTO products (
    product_id, category_id, manufacturer, model,
    product_name, specification, price, stock_quantity, is_active
) VALUES
    (1, 1, 'Lenovo', 'IdeaPad 5', 'Ноутбук Lenovo IdeaPad 5',
     'Ноутбук для работы и учёбы', 3200.00, 20, TRUE),
    (2, 1, 'ASUS', 'VivoBook 15', 'Ноутбук ASUS VivoBook 15',
     'Универсальный ноутбук', 2800.00, 15, TRUE),
    (3, 1, 'Acer', 'Aspire 7', 'Ноутбук Acer Aspire 7',
     'Ноутбук повышенной производительности', 3900.00, 10, TRUE),
    (4, 2, 'Dell', 'OptiPlex 7010', 'Компьютер Dell OptiPlex 7010',
     'Настольный офисный компьютер', 4100.00, 8, TRUE),
    (5, 2, 'HP', 'ProDesk 400', 'Компьютер HP ProDesk 400',
     'Настольный компьютер для офиса', 3600.00, 12, TRUE),
    (6, 3, 'Samsung', 'S24C', 'Монитор Samsung S24C',
     'Монитор диагональю 24 дюйма', 750.00, 25, TRUE),
    (7, 3, 'LG', '24MP', 'Монитор LG 24MP',
     'Монитор диагональю 24 дюйма', 820.00, 18, TRUE),
    (8, 4, 'Logitech', 'MK270', 'Комплект Logitech MK270',
     'Беспроводные клавиатура и мышь', 150.00, 30, TRUE),
    (9, 4, 'A4Tech', 'Bloody V7', 'Мышь A4Tech Bloody V7',
     'Проводная компьютерная мышь', 95.00, 40, TRUE);

-- 4. Продажи
INSERT INTO sales (
    sale_id, customer_id, user_id, sale_date, status, payment_type
) VALUES
    (1, 1, 2, '2026-10-10 10:00:00', 'COMPLETED', 'FULL_PAYMENT'),
    (2, 2, 2, '2026-10-11 11:30:00', 'PAID', 'FULL_PAYMENT'),
    (3, 3, 3, '2026-10-12 12:00:00', 'REGISTERED', 'INSTALLMENT'),
    (4, 4, 2, '2026-10-13 14:15:00', 'REGISTERED', 'INSTALLMENT');

-- 5. Позиции продаж
INSERT INTO sale_items (
    sale_id, line_number, product_id, quantity, unit_price
) VALUES
    (1, 1, 1, 1, 3200.00),
    (1, 2, 7, 1, 820.00),
    (2, 1, 4, 1, 4100.00),
    (3, 1, 6, 2, 750.00),
    (4, 1, 9, 2, 95.00);

-- Синхронизация последовательностей после явного задания ID.
SELECT setval(pg_get_serial_sequence('customers', 'customer_id'),
              (SELECT MAX(customer_id) FROM customers), TRUE);
SELECT setval(pg_get_serial_sequence('users', 'user_id'),
              (SELECT MAX(user_id) FROM users), TRUE);
SELECT setval(pg_get_serial_sequence('products', 'product_id'),
              (SELECT MAX(product_id) FROM products), TRUE);
SELECT setval(pg_get_serial_sequence('sales', 'sale_id'),
              (SELECT MAX(sale_id) FROM sales), TRUE);

COMMIT;