package com.latte.blockchain.entity;

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
public class LatteCoin {
    /**
     * lattecoin实例
     */
    private static LatteCoin LATTE_COIN = new LatteCoin();

    /**
     * 链
     */
    private ArrayList<Block> blockchain = new ArrayList<>();

    /**
     * 最小交易值
     */
    private final Float minimumTransaction = 0.1f;

    /**
     * 难度值
     */
    private static final Integer DIFFICULTY = 3;

    /**
     * UTXOS
     */
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    private LatteCoin() {}

    public static LatteCoin getInstance() {
        return LATTE_COIN;
    }
}
