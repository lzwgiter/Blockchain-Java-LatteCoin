package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;

import java.security.PrivateKey;

/**
 * @author float311
 * @since 2021/01/28
 */
public interface ITransactionService {

    /**
     * 为一个交易生成签名
     *
     * @param privateKey {@link PrivateKey} 签名私钥
     * @param transaction {@link Transaction} 交易
     */
    void generateSignature(PrivateKey privateKey, Transaction transaction);

    /**
     * 验证一个交易是否有效
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    boolean verifyTransaction(Transaction transaction);

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    boolean isValidSignature(Transaction transaction);

    /**
     * 计算交易哈希值
     *
     * @param transaction {@link Transaction} 交易
     * @return String
     */
    String calculateTransactionHash(Transaction transaction);
}
