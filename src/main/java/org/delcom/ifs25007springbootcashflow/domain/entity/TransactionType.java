package org.delcom.ifs25007springbootcashflow.domain.entity;

/** Jenis transaksi; sign dipakai untuk menghitung saldo. */
public enum TransactionType {
    INCOME("Pemasukan", 1),
    EXPENSE("Pengeluaran", -1);

    private final String label;
    private final int sign;

    TransactionType(String label, int sign) {
        this.label = label;
        this.sign = sign;
    }

    public String getLabel() {
        return label;
    }

    public int getSign() {
        return sign;
    }
}
