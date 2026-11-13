package org.delcom.ifs25007springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 2, 3, 4);

    private static Transaction tx(int id, long amount, TransactionType type) {
        return new Transaction(id, 1, "t" + id, amount, type, null, NOW);
    }

    @Test
    void transactionBasics() {
        Transaction t = new Transaction(1, 7, "Gaji", 100, TransactionType.INCOME, "a.png", NOW);
        assertEquals(1, t.getId());
        assertEquals(7, t.getUserId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(100, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals("a.png", t.getProofPath());
        assertEquals(NOW, t.getCreatedAt());
        assertEquals(100, t.signedAmount());
        assertTrue(t.hasProof());
        t.changeDescription("Bonus");
        t.changeAmount(5);
        t.changeProofPath("  ");
        assertEquals("Bonus", t.getDescription());
        assertEquals(5, t.getAmount());
        assertFalse(t.hasProof());
        t.changeProofPath(null);
        assertFalse(t.hasProof());
        assertEquals(-9, tx(2, 9, TransactionType.EXPENSE).signedAmount());
    }

    @Test
    void typeAndRole() {
        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.getLabel());
        assertEquals(1, TransactionType.INCOME.getSign());
        assertEquals(-1, TransactionType.EXPENSE.getSign());
        assertEquals(2, Role.values().length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    void userBasics() {
        User u = new User(1, "bos", "hash", "Bos", Role.USER, null);
        assertEquals(1, u.getId());
        assertEquals("bos", u.getUsername());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("Bos", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertNull(u.getPhotoPath());
        assertFalse(u.isAdmin());
        u.changePasswordHash("h2");
        u.changeFullName("Bos B");
        u.changePhotoPath("p.png");
        assertEquals("h2", u.getPasswordHash());
        assertEquals("Bos B", u.getFullName());
        assertEquals("p.png", u.getPhotoPath());
        assertTrue(new User(2, "a", "h", "A", Role.ADMIN, null).isAdmin());
    }

    @Test
    void sortOptions() {
        Transaction a = tx(1, 50, TransactionType.EXPENSE);
        Transaction b = tx(2, 10, TransactionType.INCOME);
        Transaction c = tx(3, 30, TransactionType.INCOME);
        assertEquals(List.of(b, c, a), sorted(SortOption.AMOUNT_ASC, a, b, c));
        assertEquals(List.of(a, c, b), sorted(SortOption.AMOUNT_DESC, a, b, c));
        assertEquals(List.of(b, c, a), sorted(SortOption.INCOME_FIRST, a, b, c));
        assertEquals(List.of(a, b, c), sorted(SortOption.EXPENSE_FIRST, a, b, c));
        for (SortOption o : SortOption.values()) {
            assertNotNull(o.getLabel());
        }
        assertEquals(4, SortOption.values().length);
    }

    private static List<Transaction> sorted(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.getComparator());
        return list;
    }

    @Test
    void exceptions() {
        assertEquals("x", new DomainException("x").getMessage());
        assertInstanceOf(DomainException.class, new ValidationException("v"));
        EntityNotFoundException e = new EntityNotFoundException("Transaksi", 9);
        assertInstanceOf(DomainException.class, e);
        assertEquals("Transaksi dengan ID 9 tidak ditemukan.", e.getMessage());
    }
}
