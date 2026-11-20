
package by.bsuir.computersales.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Модель роли пользователя системы.
 *
 * Тип роли определяется перечислением RoleType.
 */
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;
    private RoleType roleType;
    private String description;

    public Role() {
    }

    public Role(
            Long roleId,
            RoleType roleType,
            String description
    ) {
        this.roleId = roleId;
        setRoleType(roleType);
        this.description = description;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        if (roleType == null) {
            throw new IllegalArgumentException(
                    "Тип роли не может быть null"
            );
        }

        this.roleType = roleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает описание роли для отображения.
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Сравнивает роли по идентификатору.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Role role = (Role) object;

        return roleId != null && roleId.equals(role.roleId);
    }

    /**
     * Возвращает хеш-код роли на основе идентификатора.
     */
    @Override
    public int hashCode() {
        return Objects.hash(roleId);
    }
}

