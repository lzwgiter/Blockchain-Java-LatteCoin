package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;

/**
 * 交易类
 *
 * @author float
 * @since 2021/1/27
 */
@Data
@Entity
@Table(name = "transactions", schema = "lattechain")
public class Transaction {

    /**
     * 交易的id
     */
    @Id
    @Column(name = "transaction_id")
    private String id;

    /**
     * 发送方的地址
     */
    @Transient
    @JsonIgnore
    private PublicKey sender;

    /**
     * 接受方的地址
     */
    @Transient
    @JsonIgnore
    private PublicKey recipient;

    /**
     * 发起方字符串
     */
    @Getter
    @Column(name = "Sender")
    @JsonAlias({"sender"})
    private String senderString;

    /**
     * 接受方字符串
     */
    @Getter
    @JsonAlias({"recipient"})
    @Column(name = "recipient")
    private String recipientString;

    /**
     * 交易输入
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> inputUtxosId;

    /**
     * 交易输出
     */
    @Transient
    private Set<Utxo> outputUtxos;

    /**
     * 交易输出id
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> outputUtxosId;

    /**
     * 交易金额
     */
    @Column(scale = 2)
    private Float value;

    /**
     * 交易签名信息
     */
    @JsonIgnore
    private byte[] signature;

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * 数据 - 由交易双方、交易金额、时间戳组成
     */
    @JsonIgnore
    private String data;

    protected Transaction() {
    }

    /**
     * 交易
     *
     * @param sender    发送方地址
     * @param recipient 接受方地址
     * @param value     交易金额
     * @param inputs    交易输入
     */
    public Transaction(PublicKey sender, PublicKey recipient, Float value, Set<String> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputUtxosId = inputs;
        this.outputUtxosId = new HashSet<>();
        this.timeStamp = System.currentTimeMillis();
    }

    public void setData() {
        this.data = this.getSenderString() + this.getRecipientString() + value + timeStamp;
    }
}
