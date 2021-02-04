package com.latte.blockchain.enums;

import com.latte.blockchain.entity.LatteChain;

/**
 * 常量
 *
 * @author float311
 * @since 2021/01/28
 */
public class LatteChainEnum {

    /**
     * 哈希0字符串
     */
    public static final String ZERO_HASH = "0";

    /**
     * 难度字符串
     */
    public static final String TARGET_HASH = new String(
            new char[LatteChain.getDifficulty()]).replace('\0', '0');

    /**
     * 系统预置账户数量
     */
    public static final int INIT_ACCOUNT_AMOUNTS = 10;

    /**
     * 每个区块所能包含的最大交易数量
     */
    public static final int MAX_TRANSACTION_AMOUNT = 10;

    /**
     * 出块奖励：5个latte coin
     */
    public static final float SUBSIDY = 5;
}
