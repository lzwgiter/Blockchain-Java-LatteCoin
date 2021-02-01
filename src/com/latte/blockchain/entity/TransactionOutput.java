package com.latte.blockchain.entity;

import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;

/**
 * @author float
 * @since 2021/1/27
 */
public class TransactionOutput {

    /**
     * 标识
     */
    private String id;

    /**
     * 交易接受方
     */
    private PublicKey recipient;

    /**
     * 交易金额
     */
    private float value;

    /**
     * 父交易的id
     */
    private String parentTransactionId;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptoUtil.applySha256(CryptoUtil.getStringFromKey(recipient) + value + parentTransactionId);
    }

    /**
     * 检查财产是否属于该用户
     *
     * @param publicKey {@link PublicKey} 用户地址
     * @return boolean
     */
    public boolean isbelongto(PublicKey publicKey) {
        return publicKey == recipient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public void setRecipient(PublicKey recipient) {
        this.recipient = recipient;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }
}
