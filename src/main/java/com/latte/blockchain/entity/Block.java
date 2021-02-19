package com.latte.blockchain.entity;

import com.latte.blockchain.enums.LatteChainEnum;
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
     * 区块信息
     */
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
    private ArrayList<Transaction> transactions = new ArrayList<>(LatteChainEnum.MAX_TRANSACTION_AMOUNT);

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * Nonce值
     */
    private int nonce;

    public Block(String previousHash, String msg) {
        this.previousHash = previousHash;
        this.msg = msg;
        this.timeStamp = System.currentTimeMillis();
        this.nonce = 0;
    }
}
