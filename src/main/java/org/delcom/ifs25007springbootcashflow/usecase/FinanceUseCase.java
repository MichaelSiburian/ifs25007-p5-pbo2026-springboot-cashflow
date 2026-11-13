package org.delcom.ifs25007springbootcashflow.usecase;

import org.delcom.ifs25007springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25007springbootcashflow.domain.repository.ITransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Logika bisnis Cashflow per pengguna. Tanpa I/O UI. */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction addIncome(int userId, String description, String amountInput,
                                 String proofPath) throws DomainException {
        return add(userId, description, amountInput, proofPath, TransactionType.INCOME);
    }

    public Transaction addExpense(int userId, String description, String amountInput,
                                  String proofPath) throws DomainException {
        return add(userId, description, amountInput, proofPath, TransactionType.EXPENSE);
    }

    public List<Transaction> findByUser(int userId) {
        return repository.findByUserId(userId);
    }

    /** Filter keterangan (case-insensitive); tidak mengubah data. */
    public List<Transaction> search(int userId, String keyword) {
        List<Transaction> all = repository.findByUserId(userId);
        if (keyword == null || keyword.isBlank()) {
            return all;
        }
        String query = keyword.trim().toLowerCase(Locale.ROOT);
        return all.stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    /** Mengembalikan salinan terurut. */
    public List<Transaction> sort(List<Transaction> transactions, SortOption option) {
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(option.getComparator());
        return copy;
    }

    public Transaction update(int userId, int id, String description, String amountInput)
            throws DomainException {
        Transaction transaction = owned(userId, id);
        String newDescription = validDescription(description);
        long newAmount = validAmount(amountInput);
        transaction.changeDescription(newDescription);
        transaction.changeAmount(newAmount);
        repository.update(transaction);
        return transaction;
    }

    public Transaction delete(int userId, int id) throws DomainException {
        Transaction transaction = owned(userId, id);
        repository.deleteById(id);
        return transaction;
    }

    public Transaction delete(int userId, String idInput) throws DomainException {
        int id;
        try {
            id = Integer.parseInt(idInput.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException("ID tidak valid.");
        }
        return delete(userId, id);
    }

    public long getBalance(int userId) {
        return repository.findByUserId(userId).stream().mapToLong(Transaction::signedAmount).sum();
    }

    public long getTotalIncome(int userId) {
        return total(userId, TransactionType.INCOME);
    }

    public long getTotalExpense(int userId) {
        return total(userId, TransactionType.EXPENSE);
    }

    public int getTotalCount(int userId) {
        return repository.findByUserId(userId).size();
    }

    private Transaction add(int userId, String description, String amountInput, String proofPath,
                            TransactionType type) throws DomainException {
        String cleanDescription = validDescription(description);
        long amount = validAmount(amountInput);
        return repository.save(userId, cleanDescription, amount, type, proofPath);
    }

    private long total(int userId, TransactionType type) {
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getType() == type)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    private Transaction owned(int userId, int id) throws DomainException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid.");
        }
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik Anda.");
        }
        return transaction;
    }

    private String validDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong.");
        }
        return description.trim();
    }

    private long validAmount(String input) throws ValidationException {
        if (input == null || input.isBlank()) {
            throw new ValidationException("Jumlah wajib diisi.");
        }
        long amount;
        try {
            amount = Long.parseLong(input.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat (rupiah).");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0.");
        }
        return amount;
    }
}
