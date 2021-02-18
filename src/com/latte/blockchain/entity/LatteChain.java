package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.latte.blockchain.enums.LatteChainEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * LatteCoin
 *
 * @author float
 * @since 2021/1/27
 */
@Data
public class LatteChain {
    /**
     * lattecoin实例
     */
    private static LatteChain LATTE_COIN = new LatteChain();

    /**
     * 区块链
     */
    private ArrayList<Block> blockchain = new ArrayList<>();

    /**
     * 区块链中的用户信息
     */
    @JsonIgnore
    private HashMap<String, Wallet> users = new HashMap<>();

    /**
     * 最小交易值
     */
    private final float minimumTransactionValue = 0.1f;

    /**
     * UTXOS
     */
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    private LatteChain() {}

    public static LatteChain getInstance() {
        return LATTE_COIN;
    }

    public float getMinimumTransactionValue() {
        return minimumTransactionValue;
    }

    public ArrayList<Block> getBlockchain() {
        return blockchain;
    }
}
