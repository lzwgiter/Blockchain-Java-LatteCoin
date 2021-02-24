package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.latte.blockchain.utils.CryptoUtil;

import lombok.Data;

import javax.persistence.*;
import java.security.PublicKey;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
@Entity
@Table(name = "global_utxo", schema = "lattechain")
public class TransactionOutput {

    /**
     * id
     */
    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Transaction refTransaction;

    /**
     * 交易接受方
     */
    @Transient
    @JsonIgnore
    private PublicKey recipient;

    @Column(name = "recipient")
    @JsonIgnore
    private String recipientString;

    /**
     * 交易金额
     */
    private float value;

    /**
     * 时间戳
     */
    private long timeStamp;

    protected TransactionOutput() {}

    /**
     * 新建一个交易输出，并自动计算其交易ID
     *
     * @param recipient {@link PublicKey} 接受方
     * @param value     交易金额
     */
    public TransactionOutput(PublicKey recipient, float value) {
        this.recipient = recipient;
        this.recipientString = CryptoUtil.getStringFromKey(recipient);
        this.value = value;
        this.timeStamp = System.currentTimeMillis();
        this.id = CryptoUtil.applySha256(CryptoUtil.getStringFromKey(recipient) + value + timeStamp);
    }
}
