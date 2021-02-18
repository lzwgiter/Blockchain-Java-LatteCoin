package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
    ArrayList<Transaction> getTransactions();

    /**
     * 从交易池中删除一个交易信息
     *
     * @param id 索引
     */
    void removeTransaction(String id);

    /**
     * 判断当前池中是否包含该交易
     *
     * @param id 键
     * @return 是则返回true
     */
    boolean containsKey(String id);

    /**
     * 判断当前池子是否为空
     *
     * @return boolean
     */
    boolean isEmpty();
}
