package com.latte.blockchain.dao;

import com.latte.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 从表中获取4个最早的交易
     * @return {@link Transaction} 交易列表
     */
    @Query(value = "select * from transactions order by time_stamp limit 0, 5", nativeQuery = true)
    List<Transaction> getTransactions();
}
