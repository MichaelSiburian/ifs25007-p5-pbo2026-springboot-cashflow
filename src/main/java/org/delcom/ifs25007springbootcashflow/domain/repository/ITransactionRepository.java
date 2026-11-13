package org.delcom.ifs25007springbootcashflow.domain.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import java.util.List;

/** Port penyimpanan transaksi. ID dibuat otomatis oleh implementasi. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    Transaction save(int userId, String description, long amount, TransactionType type,
                     String proofPath);

    List<Transaction> findByUserId(int userId);
}
