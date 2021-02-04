package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;

import java.security.PrivateKey;
import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/01/28
 */
public interface ITransactionService {

    /**
     * 获取交易输入的总值
     *
     * @param transaction {@link Transaction} 交易
     * @return 输入总值
     */
    float getInputsValue(Transaction transaction);

    /**
     * 获取交易输出的总值
     *
     * @param transaction {@link Transaction} 交易
     * @return 输出总值
     */
    float getOutputsValue(Transaction transaction);

    /**
     * 为一个交易生成签名
     *
     * @param privateKey  {@link PrivateKey} 签名私钥
     * @param transaction {@link Transaction} 交易
     */
    void generateSignature(PrivateKey privateKey, Transaction transaction);

    /**
     * 进行交易
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    boolean processTransaction(Transaction transaction);

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
