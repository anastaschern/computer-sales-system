
package by.bsuir.computersales.client.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Модель продажи компьютерной техники.
 */
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long saleId;
    private Customer customer;
    private User user;
    private LocalDateTime saleDate;
    private SaleStatus status;
    private PaymentType paymentType;
    private List<SaleItem> items;

    public Sale() {
        this.items = new ArrayList<>();
    }

    public Sale(
            Long saleId,
            Customer customer,
            User user,
            LocalDateTime saleDate,
            SaleStatus status,
            PaymentType paymentType,
            List<SaleItem> items) {

        this();

        this.saleId = saleId;
        setCustomer(customer);
        setUser(user);
        setSaleDate(saleDate);
        setStatus(status);
        setPaymentType(paymentType);
        setItems(items);
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        if (saleId != null && saleId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор продажи должен быть положительным"
            );
        }

        this.saleId = saleId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = Objects.requireNonNull(
                customer,
                "Покупатель не должен быть null"
        );
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(
                user,
                "Сотрудник не должен быть null"
        );
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = Objects.requireNonNull(
                saleDate,
                "Дата продажи не должна быть null"
        );
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = Objects.requireNonNull(
                status,
                "Статус продажи не должен быть null"
        );
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = Objects.requireNonNull(
                paymentType,
                "Тип оплаты не должен быть null"
        );
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        Objects.requireNonNull(
                items,
                "Список товаров не должен быть null"
        );

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Список не должен содержать null"
            );
        }

        this.items = new ArrayList<>(items);
    }

    /**
     * Возвращает общую стоимость продажи.
     */
    public java.math.BigDecimal getTotalAmount() {
        return items.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal::add
                );
    }
}
