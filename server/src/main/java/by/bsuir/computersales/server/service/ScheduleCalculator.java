package by.bsuir.computersales.server.service;

import by.bsuir.computersales.common.model.InstallmentApplication;
import by.bsuir.computersales.common.model.PaymentScheduleItem;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Рассчитывает график платежей по аннуитетной схеме.
 */
public class ScheduleCalculator {

    private static final int MONEY_SCALE = 2;

    private static final RoundingMode ROUNDING_MODE =
            RoundingMode.HALF_UP;

    private static final MathContext CALCULATION_CONTEXT =
            new MathContext(20, RoundingMode.HALF_UP);

    /**
     * Формирует график платежей по одобренной заявке.
     *
     * @param application заявка на рассрочку
     * @param approvalDate дата одобрения заявки
     * @return список плановых платежей
     */
    public List<PaymentScheduleItem> calculate(
            InstallmentApplication application,
            LocalDate approvalDate) {

        Objects.requireNonNull(
                application,
                "Заявка на рассрочку не должна быть null"
        );

        Objects.requireNonNull(
                approvalDate,
                "Дата одобрения не должна быть null"
        );

        if (application.getApplicationId() == null) {
            throw new IllegalArgumentException(
                    "У заявки должен быть идентификатор"
            );
        }

        if (application.getSale() == null) {
            throw new IllegalArgumentException(
                    "У заявки должна быть указана продажа"
            );
        }

        int termMonths = application.getTermMonths();

        if (termMonths <= 0) {
            throw new IllegalArgumentException(
                    "Срок рассрочки должен быть положительным"
            );
        }

        BigDecimal totalAmount =
                application.getSale().getTotalAmount();

        BigDecimal downPayment =
                application.getDownPaymentAmount();

        BigDecimal annualInterestRate =
                application.getInterestRate();

        if (totalAmount == null
                || totalAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Стоимость продажи должна быть больше нуля"
            );
        }

        if (downPayment == null
                || downPayment.signum() < 0
                || downPayment.compareTo(totalAmount) >= 0) {
            throw new IllegalArgumentException(
                    "Некорректная сумма первоначального взноса"
            );
        }

        if (annualInterestRate == null
                || annualInterestRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Годовая процентная ставка не может быть отрицательной"
            );
        }

        BigDecimal principal = totalAmount
                .subtract(downPayment)
                .setScale(MONEY_SCALE, ROUNDING_MODE);

        BigDecimal monthlyRate = annualInterestRate.divide(
                new BigDecimal("1200"),
                CALCULATION_CONTEXT
        );

        BigDecimal monthlyPayment = calculateMonthlyPayment(
                principal,
                monthlyRate,
                termMonths
        );

        List<PaymentScheduleItem> schedule =
                new ArrayList<>(termMonths);

        BigDecimal remainingPrincipal = principal;

        for (int paymentNumber = 1;
             paymentNumber <= termMonths;
             paymentNumber++) {

            BigDecimal interest = remainingPrincipal
                    .multiply(monthlyRate, CALCULATION_CONTEXT)
                    .setScale(MONEY_SCALE, ROUNDING_MODE);

            BigDecimal plannedAmount;

            if (paymentNumber == termMonths) {
                // Последний платёж погашает весь оставшийся долг
                // с учётом процентов за последний месяц.
                plannedAmount = remainingPrincipal.add(interest)
                        .setScale(MONEY_SCALE, ROUNDING_MODE);
            } else {
                plannedAmount = monthlyPayment;

                BigDecimal principalPart =
                        plannedAmount.subtract(interest);

                if (principalPart.signum() <= 0) {
                    throw new IllegalArgumentException(
                            "Аннуитетный платёж недостаточен "
                                    + "для погашения основного долга"
                    );
                }

                remainingPrincipal = remainingPrincipal
                        .subtract(principalPart)
                        .setScale(MONEY_SCALE, ROUNDING_MODE);
            }

            if (plannedAmount.signum() <= 0) {
                throw new IllegalArgumentException(
                        "Сумма планового платежа должна быть больше нуля"
                );
            }

            LocalDate dueDate =
                    approvalDate.plusMonths(paymentNumber);

            schedule.add(
                    new PaymentScheduleItem(
                            application,
                            paymentNumber,
                            dueDate,
                            plannedAmount
                    )
            );
        }

        return schedule;
    }

    /**
     * Рассчитывает размер регулярного аннуитетного платежа.
     *
     * @param principal сумма финансирования
     * @param monthlyRate месячная процентная ставка
     * @param termMonths срок рассрочки в месяцах
     * @return размер ежемесячного платежа
     */
    private BigDecimal calculateMonthlyPayment(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int termMonths) {

        if (monthlyRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(termMonths),
                    MONEY_SCALE,
                    ROUNDING_MODE
            );
        }

        BigDecimal onePlusRate =
                BigDecimal.ONE.add(monthlyRate);

        BigDecimal discountFactor = BigDecimal.ONE.divide(
                onePlusRate.pow(termMonths, CALCULATION_CONTEXT),
                CALCULATION_CONTEXT
        );

        BigDecimal denominator =
                BigDecimal.ONE.subtract(discountFactor);

        return principal
                .multiply(monthlyRate, CALCULATION_CONTEXT)
                .divide(denominator, CALCULATION_CONTEXT)
                .setScale(MONEY_SCALE, ROUNDING_MODE);
    }
}
