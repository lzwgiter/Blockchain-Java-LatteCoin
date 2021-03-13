package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;

import java.security.PrivateKey;

/**
 * @author float311
 * @since 2021/01/28
 */
public interface ITransactionService {

    /**
     * 发起一笔交易
     *
     * @param sender    交易发起方
     * @param recipient 交易接受方
     * @param value     交易金额
     * @return String 交易信息
     */
    String createTransaction(String sender, String recipient, float value);

    /**
     * 计算交易的输出
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    boolean processTransaction(Transaction transaction);

    /**
     * 获取targetUserName作为接受方的所有交易的交易链
     *
     * @param transactionId 待审计交易id
     * @return 交易链信息
     */
    String auditTransaction(String transactionId);

    /**
     * 获取交易输入的总值
     *
     * @param transaction {@link Transaction} 交易
     * @return 输入总值
     */
    float getInputsValue(Transaction transaction);

    /**
     * 为一个交易生成签名
     *
     * @param privateKey  {@link PrivateKey} 签名私钥
     * @param transaction {@link Transaction} 交易
     */
    void generateSignature(PrivateKey privateKey, Transaction transaction);

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
