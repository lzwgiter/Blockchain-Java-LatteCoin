package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;

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
     * 创建一个新的空区块
     *
     * @param preHash 前一区块的哈希值
     * @param msg     区块附带的信息
     * @return {@link Block}
     */
    Block createNewBlock(String preHash, String msg);

    /**
     * 执行交易并将交易信息添加到区块中
     *
     * @param block       {@link Block} 区块
     * @param transaction {@link Transaction}
     * @return boolean 是否添加成功
     */
    boolean addTransaction(Block block, Transaction transaction);

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
     * @return boolean 添加成功则返回true
     */
    boolean addBlock(Block blockToAdd);

    /**
     * 检查是否是有效的区块
     *
     * @param block    当前区块
     * @return 有效则返回true
     */
    boolean isValidBlock(Block block);

    /**
     * 计算区块哈希值
     *
     * @param block 区块
     * @return String
     */
    String calculateBlockHash(Block block);
}