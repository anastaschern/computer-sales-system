
package by.bsuir.computersales.client.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель пользователя системы.
 *
 * Хеш пароля в эту модель не включается.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String login;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(
            Long userId,
            String login,
            Role role,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.login = login;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    /**
     * Возвращает строковое представление пользователя.
     */
    @Override
    public String toString() {
        return "User["
                + "userId=" + userId
                + ", login='" + login + '\''
                + ", role=" + role
                + ", active=" + active
                + ", createdAt=" + createdAt
                + ']';
    }
    /**
     * Сравнивает пользователей по идентификатору.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        User user = (User) object;

        return userId != null && userId.equals(user.userId);
    }

    /**
     * Возвращает хеш-код пользователя на основе идентификатора.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
