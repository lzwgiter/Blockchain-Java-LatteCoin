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
     * 交易池，键为交易的哈希值，值为交易对象{@link Transaction}
     */
    @Getter
    private final LinkedHashMap<String, Transaction> pool = new LinkedHashMap<>();

    private TransactionPool() {}

    public static TransactionPool getTransactionPool() {
        return TRANSACTION_POOL;
    }
}
