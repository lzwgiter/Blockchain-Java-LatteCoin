package com.latte.blockchain.repository;

import com.latte.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 交易DAO对象访问类
 *
 * @author float311
 * @since 2021/02/21
 */
public interface TransactionRepo extends JpaRepository<Transaction, String> {
    /**
     * 判断输出该id UTXO的交易是否存在， 不存在说明该UTXO为挖矿奖励
     * @param id utxo id
     * @return 是否存在
     */
    boolean existsTransactionByOutputUtxosId(String id);

    /**
     * 获取输出该id UTXO的交易
     * @param id utxo id
     * @return Transaction
     */
    Transaction getTransactionByOutputUtxosId(String id);

    /**
     * 获取指定id的交易类
     *
     * @param id 索引
     * @return Transaction
     */
    Transaction getTransactionById(String id);
}
