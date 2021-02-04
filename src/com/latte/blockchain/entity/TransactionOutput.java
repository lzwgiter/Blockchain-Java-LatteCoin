package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.latte.blockchain.utils.CryptoUtil;

import com.latte.blockchain.utils.JsonUtil;
import lombok.Data;

import java.security.PublicKey;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
public class TransactionOutput {

    /**
     * id
     */
    private String id;

    /**
     * 交易接受方
     */
    @JsonSerialize(using = JsonUtil.class)
    private PublicKey recipient;

    /**
     * 交易金额
     */
    private float value;

    /**
     * 新建一个交易输出，并自动计算其交易ID
     *
     * @param recipient {@link PublicKey} 接受方
     * @param value     交易金额
     */
    public TransactionOutput(PublicKey recipient, float value) {
        this.recipient = recipient;
        this.value = value;
        this.id = CryptoUtil.applySha256(CryptoUtil.getStringFromKey(recipient) + value);
    }

    /**
     * 检查财产是否属于该用户
     *
     * @param publicKey {@link PublicKey} 用户地址
     * @return boolean
     */
    public boolean isBelongTo(PublicKey publicKey) {
        return publicKey == recipient;
    }
}
