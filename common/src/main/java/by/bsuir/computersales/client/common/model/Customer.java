
package by.bsuir.computersales.client.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Модель покупателя.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    private String documentReference;

    public Customer() {
    }

    public Customer(
            Long customerId,
            String fullName,
            String phone,
            String email,
            String documentReference
    ) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.documentReference = documentReference;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }

    @Override
    public String toString() {
        return fullName;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Customer customer = (Customer) object;

        return customerId != null
                && customerId.equals(customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}
