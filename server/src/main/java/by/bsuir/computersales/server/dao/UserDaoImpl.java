
package by.bsuir.computersales.server.dao;

import by.bsuir.computersales.common.model.Role;
import by.bsuir.computersales.common.model.RoleType;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.server.database.DatabaseManager;
import by.bsuir.computersales.server.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-реализация доступа к данным о пользователях системы.
 */
public class UserDaoImpl implements UserDao {

    /**
     * SQL-запрос для получения всех пользователей вместе с их ролями.
     */
    private static final String FIND_ALL_SQL = """
            SELECT u.user_id,
                   u.login,
                   u.is_active,
                   u.created_at,
                   r.role_id,
                   r.role_name,
                   r.description
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            ORDER BY u.user_id
            """;

    /**
     * SQL-запрос для поиска пользователя по идентификатору.
     */
    private static final String FIND_BY_ID_SQL = """
            SELECT u.user_id,
                   u.login,
                   u.is_active,
                   u.created_at,
                   r.role_id,
                   r.role_name,
                   r.description
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.user_id = ?
            """;

    /**
     * SQL-запрос для поиска пользователя по логину.
     */
    private static final String FIND_BY_LOGIN_SQL = """
            SELECT u.user_id,
                   u.login,
                   u.is_active,
                   u.created_at,
                   r.role_id,
                   r.role_name,
                   r.description
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.login = ?
            """;

    /**
     * SQL-запрос для получения пользователя и хеша его пароля.
     * Используется только при авторизации.
     */
    private static final String FIND_CREDENTIALS_BY_LOGIN_SQL = """
            SELECT u.user_id,
                   u.login,
                   u.password_hash,
                   u.is_active,
                   u.created_at,
                   r.role_id,
                   r.role_name,
                   r.description
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.login = ?
            """;

    private final DatabaseManager databaseManager;

    /**
     * Создаёт DAO пользователей.
     *
     * @param databaseManager менеджер подключений к базе данных
     */
    public UserDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }

            return users;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить список пользователей",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор пользователя должен быть положительным"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось найти пользователя по идентификатору",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findByLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин пользователя не должен быть пустым"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_LOGIN_SQL)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось найти пользователя по логину",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserCredentials> findCredentialsByLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин пользователя не должен быть пустым"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_CREDENTIALS_BY_LOGIN_SQL)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapRow(resultSet);
                    String passwordHash = resultSet.getString("password_hash");

                    return Optional.of(
                            new UserCredentials(user, passwordHash)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Не удалось получить учётные данные пользователя",
                    exception
            );
        }
    }

    /**
     * Преобразует текущую строку результата SQL-запроса в объект User.
     *
     * @param resultSet результат SQL-запроса
     * @return объект пользователя
     * @throws SQLException при ошибке чтения данных
     */
    private User mapRow(ResultSet resultSet) throws SQLException {
        Role role = new Role(
                resultSet.getLong("role_id"),
                RoleType.valueOf(resultSet.getString("role_name")),
                resultSet.getString("description")
        );

        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("login"),
                role,
                resultSet.getBoolean("is_active"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
