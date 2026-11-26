package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.dto.CreateSaleData;
import by.bsuir.computersales.common.model.Category;
import by.bsuir.computersales.common.model.Customer;
import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.InstallmentApplicationStatus;
import by.bsuir.computersales.common.model.Laptop;
import by.bsuir.computersales.common.model.Payment;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.PaymentType;
import by.bsuir.computersales.common.model.ProductType;
import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.common.model.SaleItem;
import by.bsuir.computersales.common.model.SaleStatus;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.dao.InstallmentApplicationDao;
import by.bsuir.computersales.server.dao.PaymentDao;
import by.bsuir.computersales.server.dao.PaymentScheduleDao;
import by.bsuir.computersales.server.dao.SaleDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallmentServiceTest {

    @TempDir
    Path tempDir;

    private FakeApplicationDao applicationDao;
    private FakePaymentScheduleDao paymentScheduleDao;
    private FakePaymentDao paymentDao;
    private FakeSaleDao saleDao;

    private InstallmentService service;

    @BeforeEach
    void setUp() throws Exception {
        applicationDao = new FakeApplicationDao();
        paymentScheduleDao = new FakePaymentScheduleDao();
        paymentDao = new FakePaymentDao();
        saleDao = new FakeSaleDao();

        Path configFile = tempDir.resolve("test.properties");

        Files.writeString(configFile, """
                server.host=127.0.0.1
                server.port=8080
                server.thread-pool-size=5
                server.session-timeout-seconds=1800
                database.url=jdbc:postgresql://localhost:5432/test
                database.username=test
                database.password=test
                database.pool-size=5
                rules.term.min=3
                rules.term.max=24
                rules.down_payment.min_percent=10
                rules.financed.max=10000
                rules.active.max=2
                """);

        ServerConfig config = ServerConfig.load(configFile);

        service = new InstallmentService(
                applicationDao,
                paymentScheduleDao,
                paymentDao,
                saleDao,
                config
        );
    }

    // R1 — полнота данных заявки

    @Test
    void validateApplicationShouldReturnErrorWhenApplicationIsNull() {
        List<String> errors = service.validateApplication(null);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldReturnErrorWhenCustomerDataIsIncomplete() {
        Customer customer = createCustomer();

        customer.setFullName("");
        customer.setPhone("");
        customer.setDocumentReference("");

        InstallmentApplication application =
                createApplication(
                        createSale(1L, customer, "1000"),
                        1L
                );

        List<String> errors = service.validateApplication(application);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptCompleteApplication() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L
                );

        List<String> errors = service.validateApplication(application);

        assertTrue(errors.isEmpty());
    }

    // R2 — срок рассрочки

    @Test
    void validateApplicationShouldRejectTermBelowMinimum() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L
                );

        application.setTermMonths(2);

        List<String> errors = service.validateApplication(application);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptMinimumTerm() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L
                );

        application.setTermMonths(3);

        List<String> errors = service.validateApplication(application);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptMaximumTerm() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L
                );

        application.setTermMonths(24);

        List<String> errors = service.validateApplication(application);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldRejectTermAboveMaximum() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L
                );

        application.setTermMonths(25);

        List<String> errors = service.validateApplication(application);

        assertFalse(errors.isEmpty());
    }

    // R3 — первоначальный взнос

    @Test
    void validateApplicationShouldRejectDownPaymentBelowMinimum() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L,
                        "50"
                );

        List<String> errors = service.validateApplication(application);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptMinimumDownPayment() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "1000"),
                        1L,
                        "100"
                );

        List<String> errors = service.validateApplication(application);

        assertTrue(errors.isEmpty());
    }

    // R4 — максимальная сумма финансирования

    @Test
    void validateApplicationShouldRejectFinancedAmountAboveLimit() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "12000"),
                        1L,
                        "1000"
                );

        List<String> errors = service.validateApplication(application);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptFinancedAmountAtLimit() {
        InstallmentApplication application =
                createApplication(
                        createSale(1L, createCustomer(), "11111.13"),
                        1L,
                        "1111.13"
                );

        List<String> errors = service.validateApplication(application);

        assertTrue(errors.isEmpty());
    }

    // R5 — просроченные платежи по другим рассрочкам

    @Test
    void validateApplicationShouldRejectCustomerWithOverduePayments() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale previousSale = createSale(2L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication previousApplication =
                createApplication(previousSale, 2L);

        previousApplication.setStatus(
                InstallmentApplicationStatus.APPROVED
        );

        applicationDao.add(currentApplication);
        applicationDao.add(previousApplication);

        saleDao.setCustomerSales(
                List.of(currentSale, previousSale)
        );

        PaymentScheduleItem overdueItem =
                new PaymentScheduleItem(
                        previousApplication,
                        1,
                        LocalDate.now().minusDays(1),
                        new BigDecimal("100")
                );

        paymentScheduleDao.add(
                previousApplication.getApplicationId(),
                overdueItem
        );

        paymentDao.setPayments(
                previousApplication.getApplicationId(),
                List.of()
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptCustomerWithoutOverduePayments() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale previousSale = createSale(2L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication previousApplication =
                createApplication(previousSale, 2L);

        previousApplication.setStatus(
                InstallmentApplicationStatus.APPROVED
        );

        applicationDao.add(currentApplication);
        applicationDao.add(previousApplication);

        saleDao.setCustomerSales(
                List.of(currentSale, previousSale)
        );

        PaymentScheduleItem futureItem =
                new PaymentScheduleItem(
                        previousApplication,
                        1,
                        LocalDate.now().plusDays(1),
                        new BigDecimal("100")
                );

        paymentScheduleDao.add(
                previousApplication.getApplicationId(),
                futureItem
        );

        paymentDao.setPayments(
                previousApplication.getApplicationId(),
                List.of()
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertTrue(errors.isEmpty());
    }
    // R6 — ограничение количества действующих рассрочек

    @Test
    void validateApplicationShouldRejectWhenActiveInstallmentLimitReached() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale activeSale1 = createSale(2L, customer, "1000");
        Sale activeSale2 = createSale(3L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication activeApplication1 =
                createApplication(activeSale1, 2L);

        InstallmentApplication activeApplication2 =
                createApplication(activeSale2, 3L);

        activeApplication1.setStatus(
                InstallmentApplicationStatus.APPROVED
        );

        activeApplication2.setStatus(
                InstallmentApplicationStatus.IN_PROGRESS
        );

        applicationDao.add(currentApplication);
        applicationDao.add(activeApplication1);
        applicationDao.add(activeApplication2);

        saleDao.setCustomerSales(
                List.of(currentSale, activeSale1, activeSale2)
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldAcceptWhenActiveInstallmentLimitNotReached() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale activeSale = createSale(2L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication activeApplication =
                createApplication(activeSale, 2L);

        activeApplication.setStatus(
                InstallmentApplicationStatus.APPROVED
        );

        applicationDao.add(currentApplication);
        applicationDao.add(activeApplication);

        saleDao.setCustomerSales(
                List.of(currentSale, activeSale)
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldNotCountCurrentApplication() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale otherActiveSale = createSale(2L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication otherApplication =
                createApplication(otherActiveSale, 2L);

        // Текущая заявка уже имеет активный статус.
        currentApplication.setStatus(
                InstallmentApplicationStatus.APPROVED
        );

        otherApplication.setStatus(
                InstallmentApplicationStatus.IN_PROGRESS
        );

        applicationDao.add(currentApplication);
        applicationDao.add(otherApplication);

        saleDao.setCustomerSales(
                List.of(currentSale, otherActiveSale)
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateApplicationShouldNotCountCompletedInstallments() {
        Customer customer = createCustomer();

        Sale currentSale = createSale(1L, customer, "1000");
        Sale completedSale1 = createSale(2L, customer, "1000");
        Sale completedSale2 = createSale(3L, customer, "1000");

        InstallmentApplication currentApplication =
                createApplication(currentSale, 1L);

        InstallmentApplication completedApplication1 =
                createApplication(completedSale1, 2L);

        InstallmentApplication completedApplication2 =
                createApplication(completedSale2, 3L);

        completedApplication1.setStatus(
                InstallmentApplicationStatus.COMPLETED
        );

        completedApplication2.setStatus(
                InstallmentApplicationStatus.COMPLETED
        );

        applicationDao.add(currentApplication);
        applicationDao.add(completedApplication1);
        applicationDao.add(completedApplication2);

        saleDao.setCustomerSales(
                List.of(
                        currentSale,
                        completedSale1,
                        completedSale2
                )
        );

        List<String> errors =
                service.validateApplication(currentApplication);

        assertTrue(errors.isEmpty());
    }
    // Вспомогательные методы

    private Customer createCustomer() {
        return new Customer(
                1L,
                "Иванов Иван Иванович",
                "+375291234567",
                "test@example.com",
                "MP1234567"
        );
    }

    private User createUser() {
        Role role = new Role(
                1L,
                RoleType.SALES_MANAGER,
                "Менеджер по продажам"
        );

        return new User(
                1L,
                "test_user",
                role,
                true,
                LocalDateTime.now()
        );
    }

    private Sale createSale(
            Long saleId,
            Customer customer,
            String amount
    ) {
        Category category = new Category(
                1L,
                "Ноутбуки",
                "Категория ноутбуков",
                ProductType.LAPTOP
        );

        Laptop laptop = new Laptop(
                1L,
                category,
                "Lenovo",
                "IdeaPad",
                "Тестовый ноутбук",
                "Тестовая конфигурация",
                new BigDecimal(amount),
                10,
                true
        );

        SaleItem saleItem = new SaleItem(
                1,
                laptop,
                1,
                new BigDecimal(amount)
        );

        return new Sale(
                saleId,
                customer,
                createUser(),
                LocalDateTime.now(),
                SaleStatus.REGISTERED,
                PaymentType.INSTALLMENT,
                List.of(saleItem)
        );
    }

    private InstallmentApplication createApplication(
            Sale sale,
            Long applicationId
    ) {
        return createApplication(sale, applicationId, "100");
    }

    private InstallmentApplication createApplication(
            Sale sale,
            Long applicationId,
            String downPayment
    ) {
        return new InstallmentApplication(
                applicationId,
                sale,
                null,
                LocalDateTime.now(),
                null,
                12,
                new BigDecimal(downPayment),
                BigDecimal.ZERO,
                InstallmentApplicationStatus.CREATED,
                null
        );
    }

    // Fake DAO

    private static class FakeApplicationDao
            implements InstallmentApplicationDao {

        private final List<InstallmentApplication> applications =
                new ArrayList<>();

        void add(InstallmentApplication application) {
            applications.add(application);
        }

        @Override
        public List<InstallmentApplication> findAll() {
            return new ArrayList<>(applications);
        }

        @Override
        public Optional<InstallmentApplication> findById(
                Long applicationId
        ) {
            return applications.stream()
                    .filter(application ->
                            application.getApplicationId()
                                    .equals(applicationId))
                    .findFirst();
        }

        @Override
        public Optional<InstallmentApplication> findBySaleId(
                Long saleId
        ) {
            return applications.stream()
                    .filter(application ->
                            application.getSale().getSaleId()
                                    .equals(saleId))
                    .findFirst();
        }

        @Override
        public List<InstallmentApplication> findByStatus(
                InstallmentApplicationStatus status
        ) {
            return applications.stream()
                    .filter(application ->
                            application.getStatus() == status)
                    .toList();
        }

        @Override
        public List<InstallmentApplication> findByReviewerId(
                Long reviewerId
        ) {
            return applications.stream()
                    .filter(application ->
                            application.getReviewer() != null
                                    && application.getReviewer()
                                    .getUserId()
                                    .equals(reviewerId))
                    .toList();
        }

        @Override
        public Long create(InstallmentApplication application) {
            applications.add(application);
            return application.getApplicationId();
        }

        @Override
        public void update(InstallmentApplication application) {
            // Обновление в тестах не требуется.
        }
    }

    private static class FakePaymentScheduleDao
            implements PaymentScheduleDao {

        private final Map<Long, List<PaymentScheduleItem>> schedules =
                new HashMap<>();

        void add(
                Long applicationId,
                PaymentScheduleItem item
        ) {
            schedules.computeIfAbsent(
                    applicationId,
                    ignored -> new ArrayList<>()
            ).add(item);
        }

        @Override
        public List<PaymentScheduleItem> findByApplicationId(
                Long applicationId
        ) {
            return schedules.getOrDefault(
                    applicationId,
                    List.of()
            );
        }

        @Override
        public void create(PaymentScheduleItem item) {
            add(
                    item.getApplication().getApplicationId(),
                    item
            );
        }

        @Override
        public void createAll(List<PaymentScheduleItem> items) {
            for (PaymentScheduleItem item : items) {
                create(item);
            }
        }

        @Override
        public void deleteByApplicationId(Long applicationId) {
            schedules.remove(applicationId);
        }
    }

    private static class FakePaymentDao
            implements PaymentDao {

        private final Map<Long, List<Payment>> payments =
                new HashMap<>();

        void setPayments(
                Long applicationId,
                List<Payment> items
        ) {
            payments.put(applicationId, items);
        }

        @Override
        public List<Payment> findAll() {
            return List.of();
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            return Optional.empty();
        }

        @Override
        public List<Payment> findBySaleId(Long saleId) {
            return List.of();
        }

        @Override
        public List<Payment> findByApplicationId(
                Long applicationId
        ) {
            return payments.getOrDefault(
                    applicationId,
                    List.of()
            );
        }

        @Override
        public Long create(Payment payment) {
            return payment.getPaymentId();
        }
    }

    private static class FakeSaleDao
            implements SaleDao {

        private List<Sale> customerSales = List.of();

        void setCustomerSales(List<Sale> sales) {
            customerSales = sales;
        }

        @Override
        public List<Sale> findAll() {
            return customerSales;
        }

        @Override
        public Optional<Sale> findById(Long saleId) {
            return customerSales.stream()
                    .filter(sale ->
                            sale.getSaleId().equals(saleId))
                    .findFirst();
        }

        @Override
        public List<Sale> findByCustomerId(Long customerId) {
            return customerSales.stream()
                    .filter(sale ->
                            sale.getCustomer() != null
                                    && sale.getCustomer()
                                    .getCustomerId()
                                    .equals(customerId))
                    .toList();
        }

        @Override
        public List<Sale> findByUserId(Long userId) {
            return List.of();
        }

        @Override
        public Long createSale(
                Long userId,
                CreateSaleData saleData
        ) {
            return null;
        }
    }
}