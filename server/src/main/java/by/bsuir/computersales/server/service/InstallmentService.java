
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

    /**
     * Создаёт сервис рассрочек.
     *
     * @param applicationDao доступ к заявкам на рассрочку
     * @param paymentScheduleDao доступ к графикам платежей
     * @param paymentDao доступ к фактическим платежам
     * @param saleDao доступ к продажам
     * @param serverConfig настройки и ограничения рассрочки
     */
    public InstallmentService(
            InstallmentApplicationDao applicationDao,
            PaymentScheduleDao paymentScheduleDao,
            PaymentDao paymentDao,
            SaleDao saleDao,
            ServerConfig serverConfig
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
}
