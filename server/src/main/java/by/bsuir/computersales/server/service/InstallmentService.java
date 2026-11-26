
package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.model.Customer;
import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.server.config.ServerConfig;
import by.bsuir.computersales.server.dao.InstallmentApplicationDao;
import by.bsuir.computersales.server.dao.PaymentDao;
import by.bsuir.computersales.server.dao.PaymentScheduleDao;
import by.bsuir.computersales.server.dao.SaleDao;
import by.bsuir.computersales.common.model.Payment;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.InstallmentApplicationStatus;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import by.bsuir.computersales.server.exception.ValidationException;
import by.bsuir.computersales.common.model.RoleType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для выполнения операций с рассрочками.
 *
 * Содержит бизнес-логику создания и рассмотрения заявок,
 * формирования графиков платежей и контроля погашения рассрочек.
 */
public class InstallmentService {

    private final InstallmentApplicationDao applicationDao;
    private final PaymentScheduleDao paymentScheduleDao;
    private final PaymentDao paymentDao;
    private final SaleDao saleDao;
    private final ServerConfig serverConfig;
    private final DatabaseManager databaseManager;


    /**
     * Создаёт сервис рассрочек без возможности выполнения транзакционного
     * утверждения заявки. Используется, например, при проверке правил.
     */
    public InstallmentService(
            InstallmentApplicationDao applicationDao,
            PaymentScheduleDao paymentScheduleDao,
            PaymentDao paymentDao,
            SaleDao saleDao,
            ServerConfig serverConfig
    ) {
        this(
                applicationDao,
                paymentScheduleDao,
                paymentDao,
                saleDao,
                serverConfig,
                null
        );
    }

    /**
     * Создаёт сервис рассрочек с поддержкой транзакций.
     *
     * @param applicationDao доступ к заявкам на рассрочку
     * @param paymentScheduleDao доступ к графикам платежей
     * @param paymentDao доступ к фактическим платежам
     * @param saleDao доступ к продажам
     * @param serverConfig настройки и ограничения рассрочки
     * @param databaseManager менеджер базы данных
     */
    public InstallmentService(
            InstallmentApplicationDao applicationDao,
            PaymentScheduleDao paymentScheduleDao,
            PaymentDao paymentDao,
            SaleDao saleDao,
            ServerConfig serverConfig,
            DatabaseManager databaseManager
    ) {
        this.applicationDao = Objects.requireNonNull(
                applicationDao,
                "InstallmentApplicationDao не должен быть null"
        );

        this.paymentScheduleDao = Objects.requireNonNull(
                paymentScheduleDao,
                "PaymentScheduleDao не должен быть null"
        );

        this.paymentDao = Objects.requireNonNull(
                paymentDao,
                "PaymentDao не должен быть null"
        );

        this.saleDao = Objects.requireNonNull(
                saleDao,
                "SaleDao не должен быть null"
        );

        this.serverConfig = Objects.requireNonNull(
                serverConfig,
                "ServerConfig не должен быть null"
        );

        this.databaseManager = databaseManager;
    }

    /**
     * Проверяет полноту и корректность основных данных заявки
     * в соответствии с правилом R1.
     *
     * При обнаружении нарушений возвращает список причин,
     * по которым заявка должна быть возвращена на уточнение.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateApplicationCompleteness(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null) {
            errors.add("Заявка на рассрочку отсутствует");
            return errors;
        }

        Sale sale = application.getSale();

        if (sale == null) {
            errors.add("Не указана продажа, для которой оформляется рассрочка");
            return errors;
        }

        Customer customer = sale.getCustomer();

        if (customer == null) {
            errors.add("Не указаны данные покупателя");
        } else {
            if (customer.getFullName() == null
                    || customer.getFullName().isBlank()) {
                errors.add("Не указано ФИО покупателя");
            }

            if (customer.getPhone() == null
                    || customer.getPhone().isBlank()) {
                errors.add("Не указан телефон покупателя");
            }

            if (customer.getDocumentReference() == null
                    || customer.getDocumentReference().isBlank()) {
                errors.add("Не указаны документные данные покупателя");
            }
        }

        if (application.getTermMonths() <= 0) {
            errors.add("Не указан срок рассрочки");
        }

        BigDecimal downPayment = application.getDownPaymentAmount();

        if (downPayment == null) {
            errors.add("Не указан первоначальный взнос");
        } else {
            if (downPayment.compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Первоначальный взнос не может быть отрицательным");
            }

            BigDecimal totalAmount = sale.getTotalAmount();

            if (downPayment.compareTo(totalAmount) >= 0) {
                errors.add(
                        "Первоначальный взнос должен быть меньше стоимости продажи"
                );
            }
        }

        return errors;
    }
    /**
     * Проверяет соответствие срока рассрочки установленным ограничениям
     * в соответствии с правилом R2.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateInstallmentTerm(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null) {
            errors.add("Заявка на рассрочку отсутствует");
            return errors;
        }

        int termMonths = application.getTermMonths();
        int minTerm = serverConfig.getMinInstallmentTerm();
        int maxTerm = serverConfig.getMaxInstallmentTerm();

        if (termMonths < minTerm) {
            errors.add(
                    "Срок рассрочки меньше минимально допустимого: "
                            + minTerm + " мес."
            );
        }

        if (termMonths > maxTerm) {
            errors.add(
                    "Срок рассрочки превышает максимально допустимый: "
                            + maxTerm + " мес."
            );
        }

        return errors;
    }
    /**
     * Проверяет соответствие первоначального взноса минимальному проценту
     * от стоимости продажи в соответствии с правилом R3.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateDownPayment(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null || application.getSale() == null) {
            errors.add("Невозможно проверить первоначальный взнос: отсутствуют данные заявки или продажи");
            return errors;
        }

        BigDecimal downPayment = application.getDownPaymentAmount();

        if (downPayment == null) {
            errors.add("Не указан первоначальный взнос");
            return errors;
        }

        BigDecimal totalAmount = application.getSale().getTotalAmount();
        BigDecimal minPercent = serverConfig.getMinDownPaymentPercent();

        BigDecimal minimumDownPayment = totalAmount
                .multiply(minPercent)
                .divide(new BigDecimal("100"));

        if (downPayment.compareTo(minimumDownPayment) < 0) {
            errors.add(
                    "Первоначальный взнос меньше минимально допустимого ("
                            + minPercent.stripTrailingZeros().toPlainString()
                            + "% от стоимости продажи)"
            );
        }

        return errors;
    }
    /**
     * Проверяет, не превышает ли сумма финансирования
     * максимально допустимое значение в соответствии с правилом R4.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateFinancedAmount(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null || application.getSale() == null) {
            errors.add(
                    "Невозможно проверить сумму финансирования: "
                            + "отсутствуют данные заявки или продажи"
            );
            return errors;
        }

        BigDecimal downPayment = application.getDownPaymentAmount();

        if (downPayment == null) {
            errors.add("Не указан первоначальный взнос");
            return errors;
        }

        BigDecimal totalAmount = application.getSale().getTotalAmount();

        BigDecimal financedAmount = totalAmount.subtract(downPayment);

        BigDecimal maxFinancedAmount =
                serverConfig.getMaxFinancedAmount();

        if (financedAmount.compareTo(maxFinancedAmount) > 0) {
            errors.add(
                    "Сумма финансирования превышает максимально допустимое значение: "
                            + maxFinancedAmount.toPlainString()
            );
        }

        return errors;
    }
    /**
     * Проверяет наличие просроченных платежей по другим рассрочкам
     * покупателя в соответствии с правилом R5.
     *
     * Просроченным считается плановый платёж, срок которого наступил
     * до текущей даты, а фактически внесённая сумма меньше плановой.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateOverduePayments(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null
                || application.getSale() == null
                || application.getSale().getCustomer() == null) {
            errors.add(
                    "Невозможно проверить просроченные платежи: "
                            + "отсутствуют данные заявки или покупателя"
            );
            return errors;
        }

        Long customerId = application.getSale()
                .getCustomer()
                .getCustomerId();

        if (customerId == null) {
            errors.add(
                    "Невозможно проверить просроченные платежи: "
                            + "не определён покупатель"
            );
            return errors;
        }

        LocalDate today = LocalDate.now();

        List<Sale> customerSales = saleDao.findByCustomerId(customerId);

        for (Sale sale : customerSales) {
            if (sale == null || sale.getSaleId() == null) {
                continue;
            }

            Optional<InstallmentApplication> otherApplicationOptional =
                    applicationDao.findBySaleId(sale.getSaleId());

            if (otherApplicationOptional.isEmpty()) {
                continue;
            }

            InstallmentApplication otherApplication =
                    otherApplicationOptional.get();

            if (Objects.equals(
                    application.getApplicationId(),
                    otherApplication.getApplicationId()
            )) {
                continue;
            }

            InstallmentApplicationStatus otherStatus =
                    otherApplication.getStatus();

            if (otherStatus != InstallmentApplicationStatus.APPROVED
                    && otherStatus
                    != InstallmentApplicationStatus.IN_PROGRESS) {
                continue;
            }

            Long otherApplicationId =
                    otherApplication.getApplicationId();

            if (otherApplicationId == null) {
                continue;
            }

            List<PaymentScheduleItem> scheduleItems =
                    paymentScheduleDao.findByApplicationId(otherApplicationId);

            List<Payment> actualPayments =
                    paymentDao.findByApplicationId(otherApplicationId);

            for (PaymentScheduleItem scheduleItem : scheduleItems) {
                if (scheduleItem == null
                        || scheduleItem.getDueDate() == null
                        || scheduleItem.getPlannedAmount() == null) {
                    continue;
                }

                if (!scheduleItem.getDueDate().isBefore(today)) {
                    continue;
                }

                BigDecimal paidAmount = BigDecimal.ZERO;

                for (Payment payment : actualPayments) {
                    if (payment == null
                            || !Objects.equals(
                            payment.getPaymentNumber(),
                            scheduleItem.getPaymentNumber()
                    )
                            || payment.getAmount() == null) {
                        continue;
                    }

                    paidAmount = paidAmount.add(payment.getAmount());
                }

                if (paidAmount.compareTo(
                        scheduleItem.getPlannedAmount()
                ) < 0) {
                    errors.add(
                            "У покупателя имеются просроченные платежи "
                                    + "по другой рассрочке"
                    );
                    return errors;
                }
            }
        }

        return errors;
    }
    /**
     * Проверяет количество действующих рассрочек покупателя
     * в соответствии с правилом R6.
     *
     * Активными считаются заявки со статусами APPROVED
     * и IN_PROGRESS. Текущая проверяемая заявка исключается
     * из подсчёта.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений
     */
    private List<String> validateActiveInstallments(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        if (application == null
                || application.getSale() == null
                || application.getSale().getCustomer() == null) {
            errors.add(
                    "Невозможно проверить количество действующих рассрочек: "
                            + "отсутствуют данные заявки или покупателя"
            );
            return errors;
        }

        Long customerId = application.getSale()
                .getCustomer()
                .getCustomerId();

        if (customerId == null) {
            errors.add(
                    "Невозможно проверить количество действующих рассрочек: "
                            + "не определён покупатель"
            );
            return errors;
        }

        Long currentSaleId = application.getSale().getSaleId();

        int activeCount = 0;

        List<Sale> customerSales =
                saleDao.findByCustomerId(customerId);

        for (Sale sale : customerSales) {
            if (sale == null || sale.getSaleId() == null) {
                continue;
            }

            // Не учитываем продажу текущей заявки.
            if (Objects.equals(currentSaleId, sale.getSaleId())) {
                continue;
            }

            Optional<InstallmentApplication> otherApplicationOptional =
                    applicationDao.findBySaleId(sale.getSaleId());

            if (otherApplicationOptional.isEmpty()) {
                continue;
            }

            InstallmentApplication otherApplication =
                    otherApplicationOptional.get();

            InstallmentApplicationStatus status =
                    otherApplication.getStatus();

            if (status == InstallmentApplicationStatus.APPROVED
                    || status == InstallmentApplicationStatus.IN_PROGRESS) {
                activeCount++;
            }
        }

        int maxActiveInstallments =
                serverConfig.getMaxActiveInstallments();

        if (activeCount >= maxActiveInstallments) {
            errors.add(
                    "Превышено допустимое количество действующих рассрочек "
                            + "(максимум "
                            + maxActiveInstallments
                            + ")"
            );
        }

        return errors;
    }
    /**
     * Утверждает заявку на рассрочку и создаёт график платежей.
     *
     * Изменение статуса заявки и сохранение графика выполняются
     * в рамках одной транзакции.
     *
     * @param applicationId идентификатор заявки
     * @param reviewer сотрудник, утверждающий заявку
     * @return сформированный график платежей
     */
    public List<PaymentScheduleItem> approveApplication(
            Long applicationId,
            User reviewer
    ) {
        if (databaseManager == null) {
            throw new IllegalStateException(
                    "Для утверждения заявки необходим DatabaseManager"
            );
        }

        if (applicationId == null || applicationId <= 0) {
            throw new ValidationException(
                    "Идентификатор заявки должен быть положительным"
            );
        }

        if (reviewer == null
                || reviewer.getUserId() == null
                || reviewer.getUserId() <= 0) {
            throw new ValidationException(
                    "Необходимо указать сотрудника, утверждающего заявку"
            );
        }

        if (!reviewer.isActive()) {
            throw new BusinessRuleException(
                    "Неактивный сотрудник не может утверждать заявку"
            );
        }
        if (reviewer.getRole() == null
                || (reviewer.getRole().getRoleType() != RoleType.ADMIN
                && reviewer.getRole().getRoleType()
                != RoleType.INSTALLMENT_SPECIALIST)) {
            throw new BusinessRuleException(
                    "Недостаточно прав для утверждения заявки на рассрочку"
            );
        }
        InstallmentApplication application =
                applicationDao.findById(applicationId)
                        .orElseThrow(() -> new ValidationException(
                                "Заявка с идентификатором "
                                        + applicationId + " не найдена"
                        ));

        InstallmentApplicationStatus currentStatus =
                application.getStatus();

        if (currentStatus != InstallmentApplicationStatus.CREATED
                && currentStatus != InstallmentApplicationStatus.ON_REVIEW) {
            throw new BusinessRuleException(
                    "Можно утверждать только созданные заявки "
                            + "или заявки, находящиеся на рассмотрении"
            );
        }

        List<String> errors = validateApplication(application);

        if (!errors.isEmpty()) {
            throw new BusinessRuleException(
                    "Заявка не может быть утверждена: "
                            + String.join("; ", errors)
            );
        }

        LocalDateTime approvalDateTime = LocalDateTime.now();
        LocalDate approvalDate = approvalDateTime.toLocalDate();

        application.setReviewer(reviewer);
        application.setDecisionDate(approvalDateTime);
        application.setDecisionReason(null);
        application.setStatus(InstallmentApplicationStatus.APPROVED);

        ScheduleCalculator calculator = new ScheduleCalculator();

        List<PaymentScheduleItem> scheduleItems =
                calculator.calculate(application, approvalDate);

        return databaseManager.executeInTransaction(connection -> {
            applicationDao.update(connection, application);
            paymentScheduleDao.createAll(connection, scheduleItems);

            return scheduleItems;
        });
    }

    /**
     * Выполняет комплексную проверку заявки на рассрочку
     * по правилам R1–R6.
     *
     * @param application проверяемая заявка
     * @return список обнаруженных нарушений; пустой список означает,
     * что нарушений не обнаружено
     */
    public List<String> validateApplication(
            InstallmentApplication application
    ) {
        List<String> errors = new ArrayList<>();

        // R1 — проверка полноты данных заявки.
        errors.addAll(validateApplicationCompleteness(application));

        if (application == null) {
            return errors;
        }

        // R2 — проверка срока рассрочки.
        errors.addAll(validateInstallmentTerm(application));

        // R3 и R4 требуют наличия продажи и первоначального взноса.
        if (application.getSale() != null
                && application.getDownPaymentAmount() != null) {
            errors.addAll(validateDownPayment(application));
            errors.addAll(validateFinancedAmount(application));
        }
        // R5 — проверка просроченных платежей по другим рассрочкам.
        if (application.getSale() != null
                && application.getSale().getCustomer() != null) {
            errors.addAll(validateOverduePayments(application));
        }

        // R6 — проверка количества действующих рассрочек покупателя.
        if (application.getSale() != null
                && application.getSale().getCustomer() != null) {
            errors.addAll(validateActiveInstallments(application));
        }

        return errors;
    }
}
