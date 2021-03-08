package com.latte.blockchain.dao;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionsPoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author float311
 * @since 2021/03/08
 */
public interface TransactionPoolDao extends JpaRepository<TransactionsPoolEntity, String> {
    /**
     * 获取当前交易池中的交易数量
     *
     * @return 交易池中的可用交易的大小
     */
    @Query(value = "select count(*) from transaction_pool", nativeQuery = true)
    long getPoolSize();

    /**
     * 从表中获取4个最早的交易
     *
     * @return {@link Transaction} 交易列表
     */
    @Query(value = "select * from transaction_pool order by time_stamp limit 0, 5", nativeQuery = true)
    List<TransactionsPoolEntity> getTransactions();
}
