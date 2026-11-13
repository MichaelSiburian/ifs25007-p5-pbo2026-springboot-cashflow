package org.delcom.ifs25007springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FinancePresenterTest {
    private final FinancePresenter presenter = new FinancePresenter();

    @Test
    void formatsMoney() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("-Rp 5,500.00", presenter.formatMoney(-5500));
    }

    @Test
    void formatsTextAndMessages() {
        Transaction t = new Transaction(3, 1, "Gaji", 1000000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 7, 9, 5));
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.formatTransaction(t));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4975000));
        assertEquals("07/11/2026 09:05", presenter.formatTime(t.getCreatedAt()));
        assertEquals("Pemasukan berhasil ditambahkan: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.addSuccess(t));
        assertEquals("Transaksi berhasil diubah: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.updateSuccess(t));
        assertEquals("Transaksi berhasil dihapus: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.deleteSuccess(t));
        assertEquals("Gagal: oops", presenter.error(new ValidationException("oops")));
    }
}
