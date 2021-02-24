package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.JsonUtil;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Block refBlock;

    /**
     * 发送方的地址(公钥)
     */
    @Transient
    @JsonIgnore
    private PublicKey sender;

    /**
     * 接受方的地址(公钥)
     */
    @Transient
    @JsonIgnore
    private PublicKey recipient;

    @Getter
    @Column(name = "Sender")
    @JsonAlias({"sender"})
    private String senderString;

    @Getter
    @JsonAlias({"recipient"})
    @Column(name = "recipient")
    private String recipientString;

    /**
     * 交易输入
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> inputs;

    /**
     * 交易输出
     */
    @OneToMany(cascade = {CascadeType.REMOVE},
            mappedBy = "refTransaction",
            fetch = FetchType.EAGER)
    private List<TransactionOutput> outputs;

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
    @Column(name = "timeStamp")
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
        this.inputs = inputs;
        this.outputs = new ArrayList<>();
        this.timeStamp = System.currentTimeMillis();
        this.data = this.getSenderString() + this.getRecipientString() + value + timeStamp;
    }
}
