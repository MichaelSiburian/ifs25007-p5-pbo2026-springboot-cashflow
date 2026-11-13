package org.delcom.ifs25007springbootcashflow.adapter.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/** Penyimpanan pengguna via JDBC (PostgreSQL saat runtime). */
public class JdbcUserRepository implements IUserRepository {
    private static final String SELECT =
            "SELECT id, username, password_hash, full_name, role, photo_path FROM users";
    private static final RowMapper<User> MAPPER = (rs, i) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("photo_path"));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    public JdbcUserRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.insert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        Map<String, Object> values = new HashMap<>();
        values.put("username", username);
        values.put("password_hash", passwordHash);
        values.put("full_name", fullName);
        values.put("role", role.name());
        values.put("photo_path", null);
        int id = insert.executeAndReturnKey(values).intValue();
        return new User(id, username, passwordHash, fullName, role, null);
    }

    @Override
    public User save(User user) {
        return save(user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getRole());
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query(SELECT + " WHERE username = ?", MAPPER, username).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query(SELECT + " ORDER BY id", MAPPER);
    }

    @Override
    public void update(User user) {
        jdbc.update("UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? "
                        + "WHERE id = ?",
                user.getPasswordHash(), user.getFullName(), user.getPhotoPath(), user.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
