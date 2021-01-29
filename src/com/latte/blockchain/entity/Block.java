package com.latte.blockchain.entity;

import java.util.ArrayList;

/**
 * @author float
 * @since 2021/1/27
 */
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}
