package org.delcom.ifs25007springbootcashflow.adapter.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/** Penyimpanan transaksi via JDBC (PostgreSQL saat runtime). */
public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String SELECT = "SELECT id, user_id, description, amount, type, "
            + "proof_path, created_at FROM transactions";
    private static final RowMapper<Transaction> MAPPER = (rs, i) -> new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("description"),
            rs.getLong("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("proof_path"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.insert = new SimpleJdbcInsert(dataSource)
                .withTableName("transactions")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type,
                            String proofPath) {
        LocalDateTime createdAt = LocalDateTime.now();
        Map<String, Object> values = new HashMap<>();
        values.put("user_id", userId);
        values.put("description", description);
        values.put("amount", amount);
        values.put("type", type.name());
        values.put("proof_path", proofPath);
        values.put("created_at", Timestamp.valueOf(createdAt));
        int id = insert.executeAndReturnKey(values).intValue();
        return new Transaction(id, userId, description, amount, type, proofPath, createdAt);
    }

    @Override
    public Transaction save(Transaction t) {
        return save(t.getUserId(), t.getDescription(), t.getAmount(), t.getType(),
                t.getProofPath());
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query(SELECT + " ORDER BY id", MAPPER);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query(SELECT + " WHERE user_id = ? ORDER BY id", MAPPER, userId);
    }

    @Override
    public void update(Transaction t) {
        jdbc.update("UPDATE transactions SET description = ?, amount = ?, proof_path = ? "
                        + "WHERE id = ?",
                t.getDescription(), t.getAmount(), t.getProofPath(), t.getId());
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM transactions WHERE id = ?", id) > 0;
    }
}
