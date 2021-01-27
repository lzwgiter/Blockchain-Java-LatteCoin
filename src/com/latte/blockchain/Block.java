package com.latte.blockchain;

import com.latte.blockchain.utils.StringUtil;

import java.util.ArrayList;

/**
 *
 * @author float
 * @since 2021/1/27
 */
public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    public long timeStamp;
    public int nonce;

    private static final String ZEROHASH = "0";

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calHash();
    }

    /**
     * 计算新的区块哈希值
     *
     * @return String 新区块哈希值
     */
    public String calHash() {
        return StringUtil.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    /**
     * 模拟挖矿过程
     *
     * @param difficulty 困难值
     */
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String targetHash = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(targetHash)) {
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
        if ((!previousHash.equals(ZEROHASH))) {
            // 非初始块
            if (!transaction.processTransaction()) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}
