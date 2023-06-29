package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.latte.blockchain.utils.CryptoUtil;

import lombok.Data;

import jakarta.persistence.*;
import java.security.PublicKey;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
@Entity
@Table(name = "global_utxo", schema = "lattechain")
public class Utxo {

    /**
     * id
     */
    @Id
    @Column(name = "utxo_id")
    private String id;

    /**
     * 交易接受方
     */
    @Transient
    @JsonIgnore
    private PublicKey recipient;

    @Column(name = "owner")
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

    /**
     * 产生该UTXO的交易
     */
    private String refTransactionId;

    protected Utxo() {}

    /**
     * 新建一个交易输出，并自动计算其交易ID
     *
     * @param recipient {@link PublicKey} 接受方
     * @param value     交易金额
     */
    public Utxo(PublicKey recipient, float value) {
        this.recipient = recipient;
        this.recipientString = CryptoUtil.getStringFromKey(recipient);
        this.value = value;
        this.timeStamp = System.currentTimeMillis();
        this.id = CryptoUtil.applySm3Hash(CryptoUtil.getStringFromKey(recipient) + value + timeStamp);
    }
}
