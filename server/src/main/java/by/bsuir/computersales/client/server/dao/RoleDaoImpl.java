
package by.bsuir.computersales.client.server.dao;

import by.bsuir.computersales.client.common.model.Role;
import by.bsuir.computersales.client.common.model.RoleType;
import by.bsuir.computersales.client.server.database.DatabaseManager;
import by.bsuir.computersales.client.server.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * JDBC-реализация доступа к данным о ролях пользователей.
 */
public class RoleDaoImpl implements RoleDao {

    private static final String FIND_ALL_SQL = """
            SELECT role_id, role_name, description
            FROM roles
            ORDER BY role_id
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT role_id, role_name, description
            FROM roles
            WHERE role_id = ?
            """;

    private static final String FIND_BY_TYPE_SQL = """
            SELECT role_id, role_name, description
            FROM roles
            WHERE role_name = ?
            """;

    private final DatabaseManager databaseManager;

    public RoleDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Менеджер базы данных не должен быть null"
        );
    }

    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                roles.add(mapRow(resultSet));
            }

            return roles;

        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список ролей", e);
        }
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор роли должен быть положительным"
            );
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, roleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти роль по идентификатору", e);
        }
    }

    @Override
    public Optional<Role> findByType(RoleType roleType) {
        Objects.requireNonNull(roleType, "Тип роли не должен быть null");

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_TYPE_SQL)) {

            statement.setString(1, roleType.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти роль по типу", e);
        }
    }

    /**
     * Преобразует строку результата SQL-запроса в объект Role.
     */
    private Role mapRow(ResultSet resultSet) throws SQLException {
        return new Role(
                resultSet.getLong("role_id"),
                RoleType.valueOf(resultSet.getString("role_name")),
                resultSet.getString("description")
        );
    }
}
