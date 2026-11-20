
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.User;

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

    /**
     * Находит пользователя и хеш его пароля по логину.
     *
     * Метод предназначен для серверной авторизации.
     * Хеш пароля не включается в модель User.
     *
     * @param login логин пользователя
     * @return учётные данные или пустой Optional
     */
    Optional<UserCredentials> findCredentialsByLogin(String login);
}
