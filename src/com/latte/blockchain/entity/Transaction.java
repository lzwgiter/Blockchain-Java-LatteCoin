package com.latte.blockchain.entity;

import com.latte.blockchain.utils.CryptoUtil;
import lombok.Data;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * 交易类
 *
 * @author float
 * @since 2021/1/27
 */
@Data
public class Transaction {

    /**
     * 交易的id
     */
    private String id;

    /**
     * 发送方的地址(公钥)
     */
    private PublicKey sender;

    /**
     * 接受方的地址(公钥)
     */
    private PublicKey recipient;

    /**
     * 发送的金额
     */
    private Float value;

    /**
     * 交易签名信息
     */
    private byte[] signature;

    /**
     * 交易输入
     */
    private ArrayList<TransactionInput> inputs;

    /**
     * 交易输出
     */
    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    /**
     * 记录交易数量
     */
    private Integer sequence = 0;

    /**
     * 数据
     */
    private String data;

    /**
     * 交易
     *
     * @param sender    {@link PublicKey} 发送方地址
     * @param recipient {@link PublicKey} 接受方地址
     * @param value     交易金额
     * @param inputs    交易输入
     */
    public Transaction(PublicKey sender, PublicKey recipient, Float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = inputs;
        this.data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(recipient) + value;
    }
}
