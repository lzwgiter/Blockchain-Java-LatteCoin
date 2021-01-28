package com.latte.blockchain.entity;

import com.latte.blockchain.utils.CryptoUtil;

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

    private static final String ZEROHASH = "0";

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * 计算新的区块哈希值
     *
     * @return String 新区块哈希值
     */
    public String calHash() {
        return CryptoUtil.applySha256(
                this.previousHash +
                        this.timeStamp +
                        this.nonce +
                        this.merkleRoot
        );
    }

    /**
     * 模拟挖矿过程
     *
     * @param difficulty 困难值
     */
    public void mineBlock(int difficulty) {
        merkleRoot = CryptoUtil.getMerkleRoot(transactions);
        String targetHash = CryptoUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(targetHash)) {
            // 持续变换Nonce值，直到找到满足需求的散列值
            nonce++;
            hash = calHash();
        }
        System.out.println("[Mined √] : " + hash);
    }

    /**
     * 添加交易信息到区块
     *
     * @param transaction 交易信息
     * @return 是否添加成功
     */
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        if (!previousHash.equals(ZEROHASH)) {
            // 非初始块
            if (!transaction.verifyTransaction()) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
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
