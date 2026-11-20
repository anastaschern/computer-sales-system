
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным о пользователях системы.
 */
public interface UserDao {

    /**
     * Получает список всех пользователей.
     *
     * @return список пользователей
     */
    List<User> findAll();

    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь или пустой Optional
     */
    Optional<User> findById(Long userId);

    /**
     * Находит пользователя по логину.
     *
     * @param login логин пользователя
     * @return найденный пользователь или пустой Optional
     */
    Optional<User> findByLogin(String login);
}
