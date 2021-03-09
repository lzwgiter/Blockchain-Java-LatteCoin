package com.latte.blockchain.entity;

import com.latte.blockchain.enums.LatteChainConfEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
@Entity
@Table(name = "blocks", schema = "lattechain")
public class Block {

    /**
     * 区块id
     */
    @Id
    private long id;

    /**
     * 区块信息
     */
    @Column(name = "message", nullable = false)
    private String msg;

    /**
     * 当前区块哈希值
     */
    private String hash;

    /**
     * 前一区块哈希值
     */
    private String previousHash;

    /**
     * merkle根哈希值
     */
    private String merkleRoot;

    /**
     * 交易信息 {@link Transaction}，最多包含MAX_TRANSACTION_AMOUNT个交易信息
     */
    @OneToMany(fetch = FetchType.EAGER)
    private List<Transaction> transactions;

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * Nonce值
     */
    private int nonce;

    protected Block() {
    }

    public Block(String previousHash, String msg) {
        this.previousHash = previousHash;
        this.msg = msg;
        transactions = new ArrayList<>(LatteChainConfEnum.MAX_TRANSACTION_AMOUNT);
        this.timeStamp = System.currentTimeMillis();
        this.nonce = 0;
    }
}
