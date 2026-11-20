
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным ролей пользователей.
 *
 * Определяет операции получения ролей из базы данных.
 * Конкретная реализация будет использовать JDBC.
 */
public interface RoleDao {

    /**
     * Возвращает список всех ролей.
     *
     * @return список ролей
     */
    List<Role> findAll();

    /**
     * Ищет роль по идентификатору.
     *
     * @param roleId идентификатор роли
     * @return найденная роль или Optional.empty()
     */
    Optional<Role> findById(Long roleId);

    /**
     * Ищет роль по типу.
     *
     * @param roleType тип роли
     * @return найденная роль или Optional.empty()
     */
    Optional<Role> findByType(RoleType roleType);
}
