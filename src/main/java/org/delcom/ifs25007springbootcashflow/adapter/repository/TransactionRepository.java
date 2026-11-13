package org.delcom.ifs25007springbootcashflow.adapter.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Penyimpanan transaksi di memori dengan ID otomatis. */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    protected Integer idOf(Transaction entity) {
        return entity.getId();
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type,
                            String proofPath) {
        return save(new Transaction(sequence.getAndIncrement(), userId, description, amount, type,
                proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
