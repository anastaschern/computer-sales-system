
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Отдельный плановый платёж в графике погашения рассрочки.
 */
public class PaymentScheduleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private InstallmentApplication application;
    private int paymentNumber;
    private LocalDate dueDate;
    private BigDecimal plannedAmount;

    public PaymentScheduleItem() {
    }

    public PaymentScheduleItem(
            InstallmentApplication application,
            int paymentNumber,
            LocalDate dueDate,
            BigDecimal plannedAmount) {

        setApplication(application);
        setPaymentNumber(paymentNumber);
        setDueDate(dueDate);
        setPlannedAmount(plannedAmount);
    }

    public InstallmentApplication getApplication() {
        return application;
    }

    public void setApplication(InstallmentApplication application) {
        this.application = Objects.requireNonNull(
                application,
                "Заявка на рассрочку не должна быть null"
        );
    }

    public int getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(int paymentNumber) {
        if (paymentNumber <= 0) {
            throw new IllegalArgumentException(
                    "Номер платежа должен быть положительным"
            );
        }
        this.paymentNumber = paymentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(
                dueDate,
                "Дата платежа не должна быть null"
        );
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        if (plannedAmount == null || plannedAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Плановая сумма платежа должна быть больше нуля"
            );
        }
        this.plannedAmount = plannedAmount;
    }
}
