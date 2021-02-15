package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionPool;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.ITransactionPoolService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 交易池服务实现类
 *
 * @author float311
 * @since 2021/02/07
 */
@Service
public class TransactionPoolServiceImpl implements ITransactionPoolService {
    private final TransactionPool transactionPool = TransactionPool.getTransactionPool();

    private LinkedHashMap<Integer, Transaction> pool = transactionPool.getPool();

    /**
     * 向交易池中添加一个交易
     *
     * @param transaction {@link Transaction} 交易
     */
    @Override
    public synchronized void addTransaction(Transaction transaction) {
        transactionPool.getPool().put(getPoolSize(), transaction);
    }

    /**
     * 从交易池中获取指定数量的交易
     * opt: 1 - 10个交易；0 - 小于10的正整数个交易
     *
     * @param opt 操作数
     * @return ArrayList<Transaction>
     */
    @Override
    public synchronized ArrayList<Transaction> getTransactions(int opt) {
        if (opt == 1) {
            ArrayList<Transaction> results = new ArrayList<>(LatteChainEnum.MAX_TRANSACTION_AMOUNT);
            for (int i = 0; i < LatteChainEnum.MAX_TRANSACTION_AMOUNT; i++) {
                results.add(pool.get(i).clone());
            }
            return results;
        } else if (opt == 0) {
            ArrayList<Transaction> results = new ArrayList<>(getPoolSize());
            for (Transaction transaction : pool.values()) {
                results.add(transaction.clone());
            }
            return results;
        } else {
            return null;
        }
    }

    /**
     * 从交易池中删除一个交易信息
     *
     * @param transaction {@link Transaction}
     */
    @Override
    public synchronized void removeTransaction(Transaction transaction) {
        for (Integer index : pool.keySet()) {
            if (pool.get(index).equals(transaction)) {
                pool.remove(index);
            }
        }
    }

    @Override
    public int getPoolSize() {
        return transactionPool.getPool().size();
    }
}
