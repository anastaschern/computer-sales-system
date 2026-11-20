
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель фактически внесённого платежа.
 */
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String referenceNumber;
    private Long paymentId;
    private Sale sale;
    private InstallmentApplication application;
    private Integer paymentNumber;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentPurpose paymentPurpose;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(
            Long paymentId,
            Sale sale,
            InstallmentApplication application,
            Integer paymentNumber,
            LocalDateTime paymentDate,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentPurpose paymentPurpose,
            String referenceNumber) {

        this(
                paymentId,
                sale,
                application,
                paymentNumber,
                paymentDate,
                amount,
                paymentMethod,
                paymentPurpose
        );

        this.referenceNumber = referenceNumber;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    public Payment(
            Long paymentId,
            Sale sale,
            InstallmentApplication application,
            Integer paymentNumber,
            LocalDateTime paymentDate,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentPurpose paymentPurpose) {

        setPaymentId(paymentId);
        setSale(sale);
        setApplication(application);
        setPaymentNumber(paymentNumber);
        setPaymentDate(paymentDate);
        setAmount(amount);
        setPaymentMethod(paymentMethod);
        setPaymentPurpose(paymentPurpose);
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        if (paymentId != null && paymentId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор платежа должен быть положительным"
            );
        }
        this.paymentId = paymentId;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = Objects.requireNonNull(
                sale,
                "Продажа не должна быть null"
        );
    }

    public InstallmentApplication getApplication() {
        return application;
    }

    public void setApplication(InstallmentApplication application) {
        this.application = application;
    }

    public Integer getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(Integer paymentNumber) {
        if (paymentNumber != null && paymentNumber <= 0) {
            throw new IllegalArgumentException(
                    "Номер платежа должен быть положительным"
            );
        }
        this.paymentNumber = paymentNumber;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = Objects.requireNonNull(
                paymentDate,
                "Дата платежа не должна быть null"
        );
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Сумма платежа должна быть больше нуля"
            );
        }
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = Objects.requireNonNull(
                paymentMethod,
                "Способ оплаты не должен быть null"
        );
    }

    public PaymentPurpose getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(PaymentPurpose paymentPurpose) {
        this.paymentPurpose = Objects.requireNonNull(
                paymentPurpose,
                "Назначение платежа не должно быть null"
        );
    }

    /**
     * Проверяет согласованность назначения платежа
     * и наличия ссылки на заявку и номер платежа.
     */
    public void validatePaymentReferences() {
        boolean hasApplication = application != null;
        boolean hasPaymentNumber = paymentNumber != null;

        if (hasApplication != hasPaymentNumber) {
            throw new IllegalArgumentException(
                    "Заявка и номер платежа должны быть указаны одновременно"
            );
        }

        boolean installmentPayment =
                paymentPurpose == PaymentPurpose.INSTALLMENT_PAYMENT;

        if (installmentPayment != hasApplication) {
            throw new IllegalArgumentException(
                    "Для платежа по рассрочке необходимо указать заявку "
                            + "и номер платежа; для остальных назначений "
                            + "эти поля должны отсутствовать"
            );
        }

        if (application != null
                && application.getSale() != null
                && sale != null
                && !Objects.equals(
                application.getSale().getSaleId(),
                sale.getSaleId())) {
            throw new IllegalArgumentException(
                    "Заявка должна относиться к той же продаже, что и платёж"
            );
        }
    }
}
