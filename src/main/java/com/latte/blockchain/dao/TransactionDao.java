package com.latte.blockchain.dao;

import com.latte.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 交易DAO对象访问类
 *
 * @author float311
 * @since 2021/02/21
 */
public interface TransactionDao extends JpaRepository<Transaction, String> {
    /**
     * 获取所有收款人为recipient的交易
     *
     * @param recipient 收款人姓名
     * @return List Transactions
     */
    List<Transaction> getTransactionsByRecipientString(String recipient);

    /**
     * 获取指定id的交易类
     *
     * @param id 索引
     * @return Transaction
     */
    Transaction getTransactionById(String id);
}
