package org.delcom.ifs25007springbootcashflow.adapter.presenter;

import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format tampilan transaksi, saldo, dan pesan. */
public class FinancePresenter {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.US);

    /** Contoh: 1000000 -> "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        String number = String.format(Locale.US, "%,.2f", BigDecimal.valueOf(Math.abs(amount)));
        return amount < 0 ? "-Rp " + number : "Rp " + number;
    }

    public String formatTransaction(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | "
                + formatMoney(t.getAmount()) + " | [" + t.getType().getLabel() + "]";
    }

    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatTime(LocalDateTime time) {
        return TIME_FORMAT.format(time);
    }

    public String addSuccess(Transaction t) {
        return t.getType().getLabel() + " berhasil ditambahkan: " + formatTransaction(t);
    }

    public String updateSuccess(Transaction t) {
        return "Transaksi berhasil diubah: " + formatTransaction(t);
    }

    public String deleteSuccess(Transaction t) {
        return "Transaksi berhasil dihapus: " + formatTransaction(t);
    }

    public String error(DomainException e) {
        return "Gagal: " + e.getMessage();
    }
}
