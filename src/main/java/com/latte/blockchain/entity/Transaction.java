package com.latte.blockchain.entity;

import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.JsonUtil;

import java.security.PublicKey;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * 交易类
 *
 * @author float
 * @since 2021/1/27
 */
@Data
public class Transaction implements Cloneable {

    /**
     * 交易的id
     */
    private String id;

    /**
     * 发送方的地址(公钥)
     */
    @JsonSerialize(using = JsonUtil.class)
    private PublicKey sender;

    /**
     * 接受方的地址(公钥)
     */
    @JsonSerialize(using = JsonUtil.class)
    private PublicKey recipient;

    /**
     * 交易金额
     */
    private Float value;

    /**
     * 交易签名信息
     */
    @JsonIgnore
    private byte[] signature;

    /**
     * 交易输入
     */
    private ArrayList<TransactionInput> inputs;

    /**
     * 交易输出
     */
    private ArrayList<TransactionOutput> outputs;

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * 数据 - 由交易双方、交易金额、时间戳组成
     */
    @JsonIgnore
    private String data;

    /**
     * 交易
     *
     * @param sender    发送方地址
     * @param recipient 接受方地址
     * @param value     交易金额
     * @param inputs    交易输入
     */
    public Transaction(PublicKey sender, PublicKey recipient, Float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = inputs;
        this.outputs = new ArrayList<>();
        this.timeStamp = System.currentTimeMillis();
        this.data = this.getSenderString() + this.getRecipientString() + value + timeStamp;
    }

    public String getSenderString() {
        if (this.sender == null) {
            // 币基交易
            return "";
        } else {
            return CryptoUtil.getStringFromKey(this.sender);

        }
    }

    public String getRecipientString() {
        return CryptoUtil.getStringFromKey(this.recipient);
    }

    public boolean equals(Transaction transaction) {
        return transaction.getInputs().equals(this.inputs) && transaction.getData().equals(this.data);
    }

    @Override
    public Transaction clone() {
        try {
            Transaction cloneTransaction = (Transaction) super.clone();
            cloneTransaction.setOutputs((ArrayList<TransactionOutput>) this.outputs.clone());
            cloneTransaction.setInputs((ArrayList<TransactionInput>) this.inputs.clone());
            return cloneTransaction;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
