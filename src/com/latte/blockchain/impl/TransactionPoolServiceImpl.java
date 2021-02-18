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
    /**
     * 全局唯一的交易池对象
     */
    private final TransactionPool transactionPool = TransactionPool.getTransactionPool();

    private final LinkedHashMap<String, Transaction> pool = transactionPool.getPool();

    /**
     * 向交易池中添加一个交易
     *
     * @param transaction {@link Transaction} 交易
     */
    @Override
    public void addTransaction(Transaction transaction) {
        synchronized (pool) {
            pool.put(transaction.getId(), transaction);
            System.out.println("新交易！：" + transaction.getId());
            pool.notifyAll();
        }
    }

    /**
     * 从交易池中获取指定数量的交易
     * opt: 1 - MAX_TRANSACTION_AMOUNT个交易；
     * 0 - 小于MAX_TRANSACTION_AMOUNT的正整数个交易
     *
     * @return ArrayList<Transaction>
     */
    @Override
    public synchronized ArrayList<Transaction> getTransactions() {
        int poolSize = pool.size();
        // 池中包含交易
        ArrayList<Transaction> results;
        if (poolSize >= LatteChainEnum.MAX_TRANSACTION_AMOUNT) {
            results = new ArrayList<>(LatteChainEnum.MAX_TRANSACTION_AMOUNT);
            int count = LatteChainEnum.MAX_TRANSACTION_AMOUNT;
            for (String id : pool.keySet()) {
                if (count == 0) {
                    break;
                }
                results.add(pool.get(id).clone());
                count--;
            }
        } else {
            results = new ArrayList<>(poolSize);
            for (String id : pool.keySet()) {
                results.add(pool.get(id).clone());
            }
        }
        return results;

    }

    /**
     * 从交易池中删除一个交易信息
     *
     * @param id 索引
     */
    @Override
    public void removeTransaction(String id) {
        synchronized (pool) {
            pool.remove(id);
            System.out.println("交易：" + id + "已处理完成");
        }
    }

    /**
     * 判断当前池中是否包含该交易
     *
     * @param id 键
     * @return 是则返回true
     */
    @Override
    public boolean containsKey(String id) {
        return pool.containsKey(id);
    }

    @Override
    public boolean isEmpty() {
        return pool.isEmpty();
    }
}
