package com.latte.blockchain.entity;

import lombok.Data;

import java.util.ArrayList;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
public class Block {

    /**
     * 区块id
     */
    private Integer id;

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
     * 交易信息 {@link Transaction}，最多包含10个交易信息
     */
    private ArrayList<Transaction> transactions = new ArrayList<>(10);

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * Nonce值(计算次数)
     */
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
    }
}
