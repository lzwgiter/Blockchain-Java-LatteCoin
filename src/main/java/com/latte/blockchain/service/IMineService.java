package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;

import java.util.List;

/**
 * @author float311
 * @since 2021/2/28
 */
public interface IMineService extends Runnable{

    /**
     * 初始化LatteChain区块链
     *
     * @return 初始化成功则返回true
     */
    boolean initChain();

    /**
     * 计算新的区块哈希值并计算默克根
     *
     * @param block 前一区块的Hash值
     */
    void mineNewBlock(Block block);

    /**
     * 执行交易并将交易信息添加到区块中
     *
     * @param block       {@link Block} 区块
     * @param transactions {@link Transaction}
     * @return boolean 是否添加成功
     */
    boolean addTransaction(Block block, List<Transaction> transactions);

    /**
     * 发放奖励给旷工
     *
     * @param address 矿工账户
     * @param block   区块
     */
    void rewardMiner(String address, Block block);

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     */
    void addBlock(Block blockToAdd);

    /**
     * 计算区块哈希值
     *
     * @param block 区块
     * @return String
     */
    String calculateBlockHash(Block block);
}