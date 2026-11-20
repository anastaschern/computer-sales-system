
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель заявки на оформление рассрочки.
 */
public class InstallmentApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private Sale sale;
    private User reviewer;
    private LocalDateTime applicationDate;
    private LocalDateTime decisionDate;
    private int termMonths;
    private BigDecimal downPaymentAmount;
    private BigDecimal interestRate;
    private InstallmentApplicationStatus status;
    private String decisionReason;

    public InstallmentApplication() {
        this.applicationDate = LocalDateTime.now();
        this.downPaymentAmount = BigDecimal.ZERO;
        this.interestRate = BigDecimal.ZERO;
        this.status = InstallmentApplicationStatus.CREATED;
    }

    public InstallmentApplication(
            Long applicationId,
            Sale sale,
            User reviewer,
            LocalDateTime applicationDate,
            LocalDateTime decisionDate,
            int termMonths,
            BigDecimal downPaymentAmount,
            BigDecimal interestRate,
            InstallmentApplicationStatus status,
            String decisionReason) {

        setApplicationId(applicationId);
        setSale(sale);
        setReviewer(reviewer);
        setApplicationDate(applicationDate);
        setDecisionDate(decisionDate);
        setTermMonths(termMonths);
        setDownPaymentAmount(downPaymentAmount);
        setInterestRate(interestRate);
        setStatus(status);
        setDecisionReason(decisionReason);
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        if (applicationId != null && applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор заявки должен быть положительным"
            );
        }
        this.applicationId = applicationId;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = Objects.requireNonNull(
                sale, "Продажа не должна быть null"
        );
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = Objects.requireNonNull(
                applicationDate, "Дата заявки не должна быть null"
        );

        if (decisionDate != null && decisionDate.isBefore(applicationDate)) {
            throw new IllegalArgumentException(
                    "Дата решения не может быть раньше даты заявки"
            );
        }
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        if (decisionDate != null
                && applicationDate != null
                && decisionDate.isBefore(applicationDate)) {
            throw new IllegalArgumentException(
                    "Дата решения не может быть раньше даты заявки"
            );
        }
        this.decisionDate = decisionDate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        if (termMonths <= 0) {
            throw new IllegalArgumentException(
                    "Срок рассрочки должен быть больше нуля"
            );
        }
        this.termMonths = termMonths;
    }

    public BigDecimal getDownPaymentAmount() {
        return downPaymentAmount;
    }

    public void setDownPaymentAmount(BigDecimal downPaymentAmount) {
        if (downPaymentAmount == null || downPaymentAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Первоначальный взнос не может быть null или отрицательным"
            );
        }
        this.downPaymentAmount = downPaymentAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        if (interestRate == null || interestRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Процентная ставка не может быть null или отрицательной"
            );
        }
        this.interestRate = interestRate;
    }

    public InstallmentApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(InstallmentApplicationStatus status) {
        this.status = Objects.requireNonNull(
                status, "Статус заявки не должен быть null"
        );
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        if ((status == InstallmentApplicationStatus.REJECTED
                || status == InstallmentApplicationStatus.RETURNED)
                && (decisionReason == null || decisionReason.isBlank())) {
            throw new IllegalArgumentException(
                    "Для отклонённой или возвращённой заявки необходимо указать причину"
            );
        }
        this.decisionReason = decisionReason;
    }
}
