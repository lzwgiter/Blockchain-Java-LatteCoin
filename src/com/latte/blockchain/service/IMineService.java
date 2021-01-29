package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;

import java.util.ArrayList;

/**
 * @author float311
 * @since  2021/2/28
 */
public interface IMineService {

    /**
     * 挖新的区块
     *
     * @param difficulty 难度值
     * @param transactions 交易信息
     * @return {@link Block} 新区块
     */
    Block mineNewBlock(int difficulty, ArrayList<Transaction> transactions);

    /**
     * 为区块添加交易信息
     *
     * @param transaction {@link Transaction}
     * @return boolean 是否添加成功
     */
    boolean addTransaction(Transaction transaction);

    /**
     * 计算区块哈希值
     * @return String
     */
    String calculateBlockHash();
}