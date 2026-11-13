package org.delcom.ifs25007springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25007springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25007springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcRepositoryTest {
    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.initialize(dataSource);
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void schemaInitializerIsIdempotent() {
        assertDoesNotThrow(() -> SchemaInitializer.initialize(dataSource));
    }

    @Test
    void userCrud() {
        User a = users.save("alpha", "h1", "Alpha", Role.USER);
        User copy = users.save(new User(0, "beta", "h2", "Beta", Role.ADMIN, null));
        assertTrue(a.getId() > 0);
        assertNotEquals(a.getId(), copy.getId());
        assertEquals("Alpha", users.findById(a.getId()).orElseThrow().getFullName());
        assertTrue(users.findById(999).isEmpty());
        assertEquals(Role.ADMIN, users.findByUsername("beta").orElseThrow().getRole());
        assertTrue(users.findByUsername("zzz").isEmpty());
        assertEquals(2, users.findAll().size());

        a.changeFullName("Alpha 2");
        a.changePasswordHash("h9");
        a.changePhotoPath("p.png");
        users.update(a);
        User reloaded = users.findById(a.getId()).orElseThrow();
        assertEquals("Alpha 2", reloaded.getFullName());
        assertEquals("h9", reloaded.getPasswordHash());
        assertEquals("p.png", reloaded.getPhotoPath());

        assertTrue(users.deleteById(copy.getId()));
        assertFalse(users.deleteById(copy.getId()));
    }

    @Test
    void transactionCrud() {
        User owner = users.save("alpha", "h1", "Alpha", Role.USER);
        User other = users.save("beta", "h2", "Beta", Role.USER);
        Transaction income = transactions.save(owner.getId(), "Gaji", 5000000,
                TransactionType.INCOME, null);
        Transaction expense = transactions.save(owner.getId(), "Makan", 25000,
                TransactionType.EXPENSE, "bukti.png");
        Transaction copy = transactions.save(new Transaction(0, other.getId(), "Buku", 70000,
                TransactionType.EXPENSE, null, java.time.LocalDateTime.now()));
        assertTrue(income.getId() > 0);
        assertNotEquals(income.getId(), expense.getId());

        Transaction loaded = transactions.findById(expense.getId()).orElseThrow();
        assertEquals("Makan", loaded.getDescription());
        assertEquals(25000, loaded.getAmount());
        assertEquals(TransactionType.EXPENSE, loaded.getType());
        assertEquals("bukti.png", loaded.getProofPath());
        assertNotNull(loaded.getCreatedAt());
        assertTrue(transactions.findById(999).isEmpty());

        assertEquals(3, transactions.findAll().size());
        assertEquals(2, transactions.findByUserId(owner.getId()).size());
        assertEquals(1, transactions.findByUserId(other.getId()).size());

        income.changeDescription("Gaji Bulanan");
        income.changeAmount(6000000);
        income.changeProofPath("x.png");
        transactions.update(income);
        Transaction updated = transactions.findById(income.getId()).orElseThrow();
        assertEquals("Gaji Bulanan", updated.getDescription());
        assertEquals(6000000, updated.getAmount());
        assertEquals("x.png", updated.getProofPath());

        assertTrue(transactions.deleteById(copy.getId()));
        assertFalse(transactions.deleteById(copy.getId()));
        assertEquals(2, transactions.findAll().size());
    }
}
