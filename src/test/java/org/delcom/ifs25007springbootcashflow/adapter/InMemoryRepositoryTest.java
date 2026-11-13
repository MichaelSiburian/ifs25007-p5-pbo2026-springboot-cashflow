package org.delcom.ifs25007springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25007springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryRepositoryTest {
    @Test
    void transactionRepositoryCrud() {
        TransactionRepository repo = new TransactionRepository();
        Transaction in = repo.save(1, "Gaji", 10, TransactionType.INCOME, null);
        Transaction out = repo.save(2, "Makan", 4, TransactionType.EXPENSE, "b.png");
        assertEquals(1, in.getId());
        assertEquals(2, out.getId());
        assertTrue(repo.findById(1).isPresent());
        assertTrue(repo.findById(99).isEmpty());
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 5).size());
        in.changeDescription("Gaji Bulanan");
        repo.update(in);
        assertEquals("Gaji Bulanan", repo.findById(1).orElseThrow().getDescription());
        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findAllIsDefensiveCopy() {
        TransactionRepository repo = new TransactionRepository();
        repo.save(1, "Gaji", 10, TransactionType.INCOME, null);
        List<Transaction> copy = repo.findAll();
        copy.clear();
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void userRepositoryCrud() {
        UserRepository repo = new UserRepository();
        User a = repo.save("alpha", "h1", "Alpha", Role.USER);
        User b = repo.save("beta", "h2", "Beta", Role.ADMIN);
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals("beta", repo.findByUsername("beta").orElseThrow().getUsername());
        assertTrue(repo.findByUsername("zzz").isEmpty());
        assertEquals(2, repo.findAll().size());
        a.changeFullName("Alpha 2");
        repo.update(a);
        assertEquals("Alpha 2", repo.findById(1).orElseThrow().getFullName());
        assertTrue(repo.deleteById(2));
        assertFalse(repo.deleteById(2));
    }
}
