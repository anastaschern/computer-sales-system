
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о ролях пользователей.
 */
public interface RoleDao {

    /**
     * Получает список всех ролей.
     *
     * @return список ролей
     */
    List<Role> findAll();

    /**
     * Находит роль по идентификатору.
     *
     * @param roleId идентификатор роли
     * @return найденная роль или пустой Optional
     */
    Optional<Role> findById(Long roleId);

    /**
     * Находит роль по её типу.
     *
     * @param roleType тип роли
     * @return найденная роль или пустой Optional
     */
    Optional<Role> findByType(RoleType roleType);
}
