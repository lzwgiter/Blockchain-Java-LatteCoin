package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;

/**
 * @author float311
 * @since 2021/2/28
 */
public interface IMineService {

    /**
     * 计算新的区块哈希值并计算默克根
     *
     * @param block 前一区块的Hash值
     * @return boolean true - 成功挖掘出区块并成功添加到了区块链中
     */
    boolean mineNewBlock(Block block);

    /**
     * 为区块添加交易信息
     *
     * @param block       {@link Block} 区块
     * @param transaction {@link Transaction}
     * @return boolean 是否添加成功
     */
    boolean addTransaction(Block block, Transaction transaction);

    /**
     * 计算区块哈希值
     *
     * @param block 区块
     * @return String
     */
    String calculateBlockHash(Block block);
}