package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;

import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/02/07
 */
public interface ITransactionPoolService {

    /**
     * 添加一个交易到交易池中
     *
     * @param transaction {@link Transaction} 交易
     */
    void addTransaction(Transaction transaction);

    /**
     * 从交易池中获取指定数量的交易
     * opt: 1 - 10个交易；0 - 小于10的正整数个交易
     *
     * @return ArrayList<Transaction>
     */
    ArrayList<Transaction> getTransactions(int opt);

    /**
     * 从交易池中删除一个交易信息
     *
     * @param transaction {@link Transaction}
     */
    void removeTransaction(Transaction transaction);

    /**
     * 获取当前交易池的大小
     *
     * @return 交易池的大小
     */
    int getPoolSize();
}
