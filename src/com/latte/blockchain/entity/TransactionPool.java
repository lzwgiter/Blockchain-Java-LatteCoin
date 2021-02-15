package com.latte.blockchain.entity;

import lombok.Getter;

import java.util.LinkedHashMap;

/**
 * 全局交易池
 *
 * @author float311
 * @since 2021/02/06
 */
public class TransactionPool {

    private static final TransactionPool TRANSACTION_POOL = new TransactionPool();

    /**
     * 交易池，键为从0开始的下标，值为交易对象{@link Transaction}
     */
    @Getter
    private final LinkedHashMap<Integer, Transaction> pool = new LinkedHashMap<>();

    private TransactionPool() {
    }

    public static TransactionPool getTransactionPool() {
        return TRANSACTION_POOL;
    }
}
