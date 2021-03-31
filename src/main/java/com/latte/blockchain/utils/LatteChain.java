package com.latte.blockchain.utils;

import com.latte.blockchain.entity.Wallet;
import lombok.Data;

import java.security.PublicKey;
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
     * latteChain实例
     */
    private static LatteChain LATTE_COIN = new LatteChain();

    /**
     * latteChain实例是否已经初始化
     */
    private boolean isInit = false;

    /**
     * 区块链中的用户信息
     */
    private HashMap<String, Wallet> users = new HashMap<>();

    /**
     * 管理员公钥
     */
    private PublicKey adminPublicKey;

    /**
     * 最小交易值
     */
    private final float minimumTransactionValue = 0.1f;

    private LatteChain() {}

    public static LatteChain getInstance() {
        return LATTE_COIN;
    }
}
