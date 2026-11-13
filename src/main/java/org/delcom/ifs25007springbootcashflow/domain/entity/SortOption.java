package org.delcom.ifs25007springbootcashflow.domain.entity;

import java.util.Comparator;

/** Kriteria pengurutan transaksi; setiap opsi membawa comparator-nya. */
public enum SortOption {
    AMOUNT_ASC("Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount).thenComparing(Transaction::getId)),
    AMOUNT_DESC("Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).reversed()
                    .thenComparing(Transaction::getId)),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing(Transaction::getType).thenComparing(Transaction::getId)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing(Transaction::getType).reversed()
                    .thenComparing(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}
