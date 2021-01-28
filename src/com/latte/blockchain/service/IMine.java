package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;

import java.util.ArrayList;

public interface IMine {

    /**
     * 挖新的区块
     *
     * @param preHash 前一区块的哈希值
     * @param difficulty 难度值
     * @param transactions 交易信息
     * @return {@link Block} 新区块
     */
    Block mineNewBlock(String preHash, int difficulty, ArrayList<Transaction> transactions);

    /**
     * 为区块添加交易信息
     *
     * @param transaction {@link Transaction}
     */
    void addTransaction(Transaction transaction);

    /**
     * 计算哈希值
     * @return String
     */
    String calculateHash();



}