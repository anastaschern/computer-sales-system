package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.InstallmentApplicationStatus;
import by.bsuir.computersales.common.model.Payment;
import by.bsuir.computersales.common.model.PaymentMethod;
import by.bsuir.computersales.common.model.PaymentPurpose;
import by.bsuir.computersales.common.model.PaymentScheduleItem;
import by.bsuir.computersales.common.model.PaymentType;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.common.model.Sale;
import by.bsuir.computersales.common.model.SaleStatus;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.server.dao.InstallmentApplicationDao;
import by.bsuir.computersales.server.dao.PaymentDao;
import by.bsuir.computersales.server.dao.PaymentScheduleDao;
import by.bsuir.computersales.server.dao.SaleDao;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.BusinessRuleException;
import by.bsuir.computersales.server.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сервис регистрации и контроля фактических платежей.
 *
 * Все операции регистрации платежей выполняются
 * в рамках одной транзакции.
 */
public class PaymentService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private final PaymentDao paymentDao;
    private final PaymentScheduleDao paymentScheduleDao;
    private final InstallmentApplicationDao applicationDao;
    private final SaleDao saleDao;
    private final DatabaseManager databaseManager;

    public PaymentService(
            PaymentDao paymentDao,
            PaymentScheduleDao paymentScheduleDao,
            InstallmentApplicationDao applicationDao,
            SaleDao saleDao,
            DatabaseManager databaseManager
    ) {
        this.paymentDao = Objects.requireNonNull(
                paymentDao,
                "PaymentDao не должен быть null"
        );

        this.paymentScheduleDao = Objects.requireNonNull(
                paymentScheduleDao,
                "PaymentScheduleDao не должен быть null"
        );

        this.applicationDao = Objects.requireNonNull(
                applicationDao,
                "InstallmentApplicationDao не должен быть null"
        );

        this.saleDao = Objects.requireNonNull(
                saleDao,
                "SaleDao не должен быть null"
        );

        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "DatabaseManager не должен быть null"
        );
    }

    /**
     * Регистрирует платёж с проверкой полномочий сотрудника.
     *
     * Используется для полной оплаты и платежей по графику.
     *
     * @param payment регистрируемый платёж
     * @param operator сотрудник, регистрирующий платёж
     * @return зарегистрированный платёж с идентификатором
     */
    public Payment registerPayment(
            Payment payment,
            User operator
    ) {
        validateOperator(operator);
        validatePayment(payment);

        if (payment.getPaymentPurpose() == PaymentPurpose.DOWN_PAYMENT) {
            throw new ValidationException(
                    "Для регистрации первоначального взноса "
                            + "необходимо передать идентификатор заявки"
            );
        }

        payment.setAmount(normalizeAmount(payment.getAmount()));

        return databaseManager.executeInTransaction(connection -> {
            if (payment.getPaymentPurpose() == PaymentPurpose.FULL_PAYMENT) {
                return registerFullPayment(connection, payment);
            }

            return registerInstallmentPayment(
                    connection,
                    payment,
                    payment.getApplication().getApplicationId()
            );
        });
    }

    /**
     * Регистрирует первоначальный взнос по заявке на рассрочку.
     *
     * Идентификатор заявки передаётся отдельно, поскольку
     * первоначальный взнос не содержит application_id в таблице payments.
     *
     * @param payment регистрируемый первоначальный взнос
     * @param operator сотрудник, регистрирующий платёж
     * @param applicationId идентификатор заявки на рассрочку
     * @return зарегистрированный платёж
     */
    public Payment registerPayment(
            Payment payment,
            User operator,
            Long applicationId
    ) {
        validateOperator(operator);
        validatePayment(payment);

        if (payment.getPaymentPurpose() != PaymentPurpose.DOWN_PAYMENT) {
            throw new ValidationException(
                    "Этот метод предназначен только для первоначального взноса"
            );
        }

        if (applicationId == null || applicationId <= 0) {
            throw new ValidationException(
                    "Необходимо указать корректный идентификатор заявки"
            );
        }

        payment.setAmount(normalizeAmount(payment.getAmount()));

        return databaseManager.executeInTransaction(connection ->
                registerInstallmentPayment(
                        connection,
                        payment,
                        applicationId
                )
        );
    }
    /**
     * Проверяет сотрудника, выполняющего операцию.
     */
    private void validateOperator(User operator) {

        if (operator == null
                || operator.getUserId() == null
                || operator.getUserId() <= 0) {
            throw new ValidationException(
                    "Необходимо указать сотрудника"
            );
        }

        if (!operator.isActive()) {
            throw new BusinessRuleException(
                    "Неактивный сотрудник не может регистрировать платежи"
            );
        }

        if (operator.getRole() == null
                || operator.getRole().getRoleType() == null) {
            throw new BusinessRuleException(
                    "У сотрудника не определена роль"
            );
        }

        RoleType roleType = operator.getRole().getRoleType();

        if (roleType != RoleType.ADMIN
                && roleType != RoleType.SALES_MANAGER) {
            throw new BusinessRuleException(
                    "Недостаточно прав для регистрации платежей"
            );
        }
    }

    /**
     * Проверяет общие данные платежа.
     */
    private void validatePayment(Payment payment) {

        if (payment == null) {
            throw new ValidationException(
                    "Данные платежа не должны быть null"
            );
        }

        if (payment.getPaymentId() != null) {
            throw new ValidationException(
                    "Новый платёж не должен иметь идентификатор"
            );
        }

        if (payment.getSale() == null
                || payment.getSale().getSaleId() == null
                || payment.getSale().getSaleId() <= 0) {
            throw new ValidationException(
                    "Необходимо указать продажу"
            );
        }

        if (payment.getAmount() == null
                || payment.getAmount().signum() <= 0) {
            throw new ValidationException(
                    "Сумма платежа должна быть больше нуля"
            );
        }

        if (payment.getPaymentMethod() == null) {
            throw new ValidationException(
                    "Необходимо указать способ оплаты"
            );
        }

        if (payment.getPaymentPurpose() == null) {
            throw new ValidationException(
                    "Необходимо указать назначение платежа"
            );
        }

        if (payment.getPaymentDate() == null) {
            throw new ValidationException(
                    "Необходимо указать дату платежа"
            );
        }

        if (payment.getPaymentDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException(
                    "Дата платежа не может быть в будущем"
            );
        }

        if (payment.getPaymentPurpose()
                == PaymentPurpose.INSTALLMENT_PAYMENT) {

            if (payment.getApplication() == null
                    || payment.getApplication().getApplicationId() == null) {
                throw new ValidationException(
                        "Для платежа по графику необходимо указать заявку"
                );
            }

            if (payment.getPaymentNumber() == null
                    || payment.getPaymentNumber() <= 0) {
                throw new ValidationException(
                        "Необходимо указать номер платежа по графику"
                );
            }
        } else {
            if (payment.getApplication() != null
                    || payment.getPaymentNumber() != null) {
                throw new ValidationException(
                        "Для данного назначения платежа "
                                + "заявка и номер платежа не указываются"
                );
            }
        }
    }

    /**
     * Приводит сумму к точности денежного значения.
     * Значения с более чем двумя значащими десятичными знаками
     * отклоняются, а не округляются.
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ValidationException(
                    "Сумма платежа должна содержать не более двух "
                            + "знаков после запятой"
            );
        }
    }

    /**
     * Регистрирует оплату обычной продажи.
     */
    private Payment registerFullPayment(
            java.sql.Connection connection,
            Payment payment
    ) {

        Long saleId = payment.getSale().getSaleId();

        // Блокировка продажи предотвращает параллельную переплату.
        saleDao.lockById(connection, saleId);

        Sale sale = saleDao.findById(saleId)
                .orElseThrow(() -> new ValidationException(
                        "Продажа не найдена"
                ));

        validateSaleForPayment(sale);

        if (sale.getPaymentType() != PaymentType.FULL_PAYMENT) {
            throw new BusinessRuleException(
                    "Полная оплата допускается только "
                            + "для продажи с типом FULL_PAYMENT"
            );
        }

        if (sale.getStatus() != SaleStatus.REGISTERED) {
            throw new BusinessRuleException(
                    "Оплата возможна только зарегистрированной продажи"
            );
        }

        List<Payment> existingPayments =
                paymentDao.findBySaleId(saleId);

        BigDecimal alreadyPaid = sumPayments(
                existingPayments,
                PaymentPurpose.FULL_PAYMENT,
                null
        );

        BigDecimal totalAmount = normalizeAmount(
                sale.getTotalAmount()
        );

        BigDecimal remaining = totalAmount.subtract(alreadyPaid);

        if (remaining.signum() <= 0) {
            throw new BusinessRuleException(
                    "Продажа уже полностью оплачена"
            );
        }

        if (payment.getAmount().compareTo(remaining) > 0) {
            throw new BusinessRuleException(
                    "Сумма платежа превышает остаток задолженности. "
                            + "Остаток: " + remaining.toPlainString()
            );
        }

        payment.setSale(sale);

        Long paymentId = paymentDao.create(connection, payment);
        payment.setPaymentId(paymentId);

        BigDecimal projectedPaid =
                alreadyPaid.add(payment.getAmount());

        if (projectedPaid.compareTo(totalAmount) == 0) {
            saleDao.updateStatus(
                    connection,
                    saleId,
                    SaleStatus.PAID
            );
        }

        return payment;
    }


    /**
     * Регистрирует первоначальный взнос
     * или очередной платёж по рассрочке.
     */
    private Payment registerInstallmentPayment(
            java.sql.Connection connection,
            Payment payment,
            Long applicationId
    ) {
        if (applicationId == null || applicationId <= 0) {
            throw new ValidationException(
                    "Необходимо указать корректный идентификатор заявки"
            );
        }

        // Сериализуем все платежи по одной заявке.
        applicationDao.lockById(connection, applicationId);

        InstallmentApplication application =
                applicationDao.findById(applicationId)
                        .orElseThrow(() -> new ValidationException(
                                "Заявка на рассрочку не найдена"
                        ));

        Sale applicationSale = application.getSale();

        if (applicationSale == null
                || applicationSale.getSaleId() == null) {
            throw new ValidationException(
                    "У заявки отсутствует продажа"
            );
        }

        if (!Objects.equals(
                applicationSale.getSaleId(),
                payment.getSale().getSaleId()
        )) {
            throw new BusinessRuleException(
                    "Указанная заявка не относится к выбранной продаже"
            );
        }

        Sale sale = saleDao.findById(applicationSale.getSaleId())
                .orElseThrow(() -> new ValidationException(
                        "Продажа заявки не найдена"
                ));

        validateSaleForPayment(sale);

        if (sale.getPaymentType() != PaymentType.INSTALLMENT) {
            throw new BusinessRuleException(
                    "Платежи по рассрочке возможны только "
                            + "для продажи типа INSTALLMENT"
            );
        }

        if (sale.getStatus() != SaleStatus.REGISTERED) {
            throw new BusinessRuleException(
                    "Платежи возможны только по зарегистрированной продаже"
            );
        }

        if (application.getStatus()
                == InstallmentApplicationStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Рассрочка уже полностью погашена"
            );
        }

        if (payment.getPaymentPurpose() == PaymentPurpose.DOWN_PAYMENT) {
            return registerDownPayment(
                    connection,
                    payment,
                    application,
                    sale
            );
        }

        return registerSchedulePayment(
                connection,
                payment,
                application,
                sale
        );
    }

    /**
     * Проверяет состояние продажи.
     */
    private void validateSaleForPayment(Sale sale) {

        if (sale == null) {
            throw new ValidationException(
                    "Продажа не найдена"
            );
        }

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Нельзя регистрировать платеж по закрытой продаже"
            );
        }

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Нельзя регистрировать платеж по отменённой продаже"
            );
        }

        if (sale.getStatus() == SaleStatus.PAID) {
            throw new BusinessRuleException(
                    "Продажа уже полностью оплачена"
            );
        }
    }

    /**
     * Регистрирует первоначальный взнос.
     * Допускается внесение взноса частями.
     */
    private Payment registerDownPayment(
            java.sql.Connection connection,
            Payment payment,
            InstallmentApplication application,
            Sale sale
    ) {

        if (application.getStatus()
                != InstallmentApplicationStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Первоначальный взнос можно вносить "
                            + "только по одобренной заявке"
            );
        }

        List<Payment> existingPayments =
                paymentDao.findBySaleId(sale.getSaleId());

        BigDecimal alreadyPaid = sumPayments(
                existingPayments,
                PaymentPurpose.DOWN_PAYMENT,
                null
        );

        BigDecimal requiredAmount = normalizeAmount(
                application.getDownPaymentAmount()
        );

        BigDecimal remaining =
                requiredAmount.subtract(alreadyPaid);

        if (remaining.signum() <= 0) {
            throw new BusinessRuleException(
                    "Первоначальный взнос уже полностью оплачен"
            );
        }

        if (payment.getAmount().compareTo(remaining) > 0) {
            throw new BusinessRuleException(
                    "Сумма платежа превышает остаток первоначального взноса. "
                            + "Остаток: " + remaining.toPlainString()
            );
        }

        payment.setSale(sale);

        Long paymentId = paymentDao.create(connection, payment);
        payment.setPaymentId(paymentId);

        // После первоначального взноса статус остаётся APPROVED.
        // IN_PROGRESS устанавливается при первом платеже по графику.

        return payment;
    }

    /**
     * Регистрирует очередной платёж по графику рассрочки.
     */
    private Payment registerSchedulePayment(
            java.sql.Connection connection,
            Payment payment,
            InstallmentApplication application,
            Sale sale
    ) {

        InstallmentApplicationStatus applicationStatus =
                application.getStatus();

        if (applicationStatus
                != InstallmentApplicationStatus.APPROVED
                && applicationStatus
                != InstallmentApplicationStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Платежи по графику возможны только "
                            + "по одобренной или действующей рассрочке"
            );
        }

        List<Payment> existingPayments =
                paymentDao.findBySaleId(sale.getSaleId());

        // Первоначальный взнос должен быть полностью оплачен.
        BigDecimal downPaymentPaid = sumPayments(
                existingPayments,
                PaymentPurpose.DOWN_PAYMENT,
                null
        );

        BigDecimal requiredDownPayment = normalizeAmount(
                application.getDownPaymentAmount()
        );

        if (downPaymentPaid.compareTo(requiredDownPayment) < 0) {
            throw new BusinessRuleException(
                    "Нельзя оплачивать график до полного внесения "
                            + "первоначального взноса. Остаток: "
                            + requiredDownPayment
                            .subtract(downPaymentPaid)
                            .toPlainString()
            );
        }

        List<PaymentScheduleItem> schedule =
                paymentScheduleDao.findByApplicationId(
                        application.getApplicationId()
                );

        if (schedule == null || schedule.isEmpty()) {
            throw new BusinessRuleException(
                    "График платежей не сформирован"
            );
        }

        schedule = schedule.stream()
                .sorted(Comparator.comparingInt(
                        PaymentScheduleItem::getPaymentNumber
                ))
                .toList();

        Map<Integer, BigDecimal> paidByNumber =
                calculatePaidByScheduleNumber(existingPayments);

        PaymentScheduleItem firstUnpaid = null;

        for (PaymentScheduleItem item : schedule) {

            BigDecimal paid = paidByNumber.getOrDefault(
                    item.getPaymentNumber(),
                    ZERO
            );

            BigDecimal planned = normalizeAmount(
                    item.getPlannedAmount()
            );

            if (paid.compareTo(planned) < 0) {
                firstUnpaid = item;
                break;
            }
        }

        if (firstUnpaid == null) {
            throw new BusinessRuleException(
                    "Все платежи по графику уже оплачены"
            );
        }

        if (!Objects.equals(
                payment.getPaymentNumber(),
                firstUnpaid.getPaymentNumber()
        )) {
            throw new BusinessRuleException(
                    "Нельзя пропускать платежи по графику. "
                            + "Следующий платеж: №"
                            + firstUnpaid.getPaymentNumber()
            );
        }

        BigDecimal plannedAmount = normalizeAmount(
                firstUnpaid.getPlannedAmount()
        );

        BigDecimal alreadyPaid = paidByNumber.getOrDefault(
                firstUnpaid.getPaymentNumber(),
                ZERO
        );

        BigDecimal remaining =
                plannedAmount.subtract(alreadyPaid);

        if (payment.getAmount().compareTo(remaining) > 0) {
            throw new BusinessRuleException(
                    "Сумма платежа превышает остаток платежа №"
                            + firstUnpaid.getPaymentNumber()
                            + ". Остаток: "
                            + remaining.toPlainString()
            );
        }

        payment.setSale(sale);
        payment.setApplication(application);

        Long paymentId = paymentDao.create(connection, payment);
        payment.setPaymentId(paymentId);

        BigDecimal projectedPaidForCurrent =
                alreadyPaid.add(payment.getAmount());

        paidByNumber.put(
                firstUnpaid.getPaymentNumber(),
                projectedPaidForCurrent
        );

        // Начало погашения графика переводит заявку
        // в статус IN_PROGRESS.
        if (applicationStatus
                == InstallmentApplicationStatus.APPROVED) {
            application.setStatus(
                    InstallmentApplicationStatus.IN_PROGRESS
            );
        }

        boolean scheduleCompleted = schedule.stream()
                .allMatch(item -> paidByNumber.getOrDefault(
                        item.getPaymentNumber(),
                        ZERO
                ).compareTo(normalizeAmount(item.getPlannedAmount())) >= 0);

        if (scheduleCompleted) {

            application.setStatus(
                    InstallmentApplicationStatus.COMPLETED
            );

            applicationDao.update(
                    connection,
                    application
            );

            saleDao.updateStatus(
                    connection,
                    sale.getSaleId(),
                    SaleStatus.PAID
            );

        } else if (applicationStatus
                != InstallmentApplicationStatus.IN_PROGRESS) {

            applicationDao.update(
                    connection,
                    application
            );
        }

        return payment;
    }

    /**
     * Рассчитывает суммы оплаченных платежей
     * по каждому номеру графика.
     */
    private Map<Integer, BigDecimal> calculatePaidByScheduleNumber(
            List<Payment> payments
    ) {

        Map<Integer, BigDecimal> result = new HashMap<>();

        if (payments == null) {
            return result;
        }

        for (Payment payment : payments) {

            if (payment == null
                    || payment.getPaymentPurpose()
                    != PaymentPurpose.INSTALLMENT_PAYMENT
                    || payment.getPaymentNumber() == null
                    || payment.getAmount() == null) {
                continue;
            }

            int paymentNumber = payment.getPaymentNumber();

            BigDecimal current = result.getOrDefault(
                    paymentNumber,
                    ZERO
            );

            result.put(
                    paymentNumber,
                    current.add(payment.getAmount())
            );
        }

        return result;
    }

    /**
     * Суммирует платежи с заданным назначением.
     *
     * При необходимости учитывает номер платежа по графику.
     */
    private BigDecimal sumPayments(
            List<Payment> payments,
            PaymentPurpose purpose,
            Integer paymentNumber
    ) {

        BigDecimal result = ZERO;

        if (payments == null) {
            return result;
        }

        for (Payment payment : payments) {

            if (payment == null
                    || payment.getAmount() == null
                    || payment.getPaymentPurpose() != purpose) {
                continue;
            }

            if (paymentNumber != null
                    && !Objects.equals(
                    payment.getPaymentNumber(),
                    paymentNumber
            )) {
                continue;
            }

            result = result.add(payment.getAmount());
        }

        return result;
    }
}