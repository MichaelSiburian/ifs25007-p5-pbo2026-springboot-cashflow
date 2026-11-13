package org.delcom.ifs25007springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25007springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FinanceUseCaseTest {
    private static final int U1 = 1;
    private static final int U2 = 2;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addAndBalanceAreScopedPerUser() throws Exception {
        Transaction in = useCase.addIncome(U1, "  Gaji  ", " 5000000 ", "bukti.png");
        useCase.addExpense(U1, "Makan", "25000", null);
        useCase.addExpense(U2, "Buku", "70000", null);
        assertEquals("Gaji", in.getDescription());
        assertEquals(5000000, in.getAmount());
        assertEquals("bukti.png", in.getProofPath());
        assertEquals(4975000, useCase.getBalance(U1));
        assertEquals(5000000, useCase.getTotalIncome(U1));
        assertEquals(25000, useCase.getTotalExpense(U1));
        assertEquals(2, useCase.getTotalCount(U1));
        assertEquals(2, useCase.findByUser(U1).size());
        assertEquals(-70000, useCase.getBalance(U2));
        assertEquals(0, useCase.getBalance(99));
    }

    @Test
    void descriptionValidation() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, null, "10", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(U1, "  ", "10", null));
    }

    @Test
    void amountValidation() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, "a", null, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, "a", " ", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, "a", "abc", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, "a", "10.5", null));
        assertThrows(ValidationException.class,
                () -> useCase.addIncome(U1, "a", "99999999999999999999", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(U1, "a", "0", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(U1, "a", "-5", null));
        assertEquals(0, useCase.getTotalCount(U1));
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotChangeData() throws Exception {
        useCase.addIncome(U1, "Gaji Bulanan", "100", null);
        useCase.addExpense(U1, "Makan siang", "10", null);
        useCase.addExpense(U2, "Gaji orang lain", "10", null);
        assertEquals(2, useCase.search(U1, null).size());
        assertEquals(2, useCase.search(U1, "  ").size());
        List<Transaction> found = useCase.search(U1, " GAJI ");
        assertEquals(1, found.size());
        assertEquals("Gaji Bulanan", found.get(0).getDescription());
        assertTrue(useCase.search(U1, "zzz").isEmpty());
        assertEquals(2, useCase.getTotalCount(U1));
    }

    @Test
    void sortReturnsSortedCopy() throws Exception {
        useCase.addIncome(U1, "a", "30", null);
        useCase.addExpense(U1, "b", "10", null);
        List<Transaction> original = useCase.findByUser(U1);
        List<Transaction> sorted = useCase.sort(original, SortOption.AMOUNT_ASC);
        assertEquals("b", sorted.get(0).getDescription());
        assertEquals("a", original.get(0).getDescription());
    }

    @Test
    void updateChecksOwnershipAndInput() throws Exception {
        Transaction t = useCase.addIncome(U1, "Gaji", "100", null);
        Transaction updated = useCase.update(U1, t.getId(), "Gaji Baru", "200");
        assertEquals("Gaji Baru", updated.getDescription());
        assertEquals(200, useCase.findByUser(U1).get(0).getAmount());
        assertThrows(ValidationException.class, () -> useCase.update(U1, 0, "x", "1"));
        assertThrows(EntityNotFoundException.class, () -> useCase.update(U1, 99, "x", "1"));
        assertThrows(ValidationException.class, () -> useCase.update(U2, t.getId(), "x", "1"));
        assertThrows(ValidationException.class, () -> useCase.update(U1, t.getId(), " ", "1"));
        assertThrows(ValidationException.class, () -> useCase.update(U1, t.getId(), "x", "abc"));
        assertEquals("Gaji Baru", useCase.findByUser(U1).get(0).getDescription());
    }

    @Test
    void deleteById() throws Exception {
        Transaction t = useCase.addIncome(U1, "a", "30", null);
        assertThrows(ValidationException.class, () -> useCase.delete(U1, 0));
        assertThrows(EntityNotFoundException.class, () -> useCase.delete(U1, 99));
        assertThrows(ValidationException.class, () -> useCase.delete(U2, t.getId()));
        assertSame(t, useCase.delete(U1, t.getId()));
        assertEquals(0, useCase.getTotalCount(U1));
    }

    @Test
    void deleteByText() throws Exception {
        useCase.addIncome(U1, "a", "30", null);
        assertThrows(ValidationException.class, () -> useCase.delete(U1, "abc"));
        assertThrows(ValidationException.class, () -> useCase.delete(U1, (String) null));
        assertThrows(EntityNotFoundException.class, () -> useCase.delete(U1, "42"));
        assertEquals("a", useCase.delete(U1, " 1 ").getDescription());
    }
}
