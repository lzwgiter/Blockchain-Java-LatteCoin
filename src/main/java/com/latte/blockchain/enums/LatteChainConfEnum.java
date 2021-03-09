package com.latte.blockchain.enums;

/**
 * 常量
 *
 * @author float311
 * @since 2021/01/28
 */
public class LatteChainConfEnum {

    /**
     * 哈希0字符串
     */
    public static final String ZERO_HASH = "0";

    /**
     * 难度值
     */
    public static final Integer DIFFICULTY = 4;

    /**
     * 难度字符串
     */
    public static final String TARGET_HASH = new String(
            new char[DIFFICULTY]).replace('\0', '0');

    /**
     * 系统预置账户数量
     */
    public static final int INIT_ACCOUNT_AMOUNTS = 5;

    /**
     * 每个区块所能包含的最大交易数量
     */
    public static final int MAX_TRANSACTION_AMOUNT = 4;

    /**
     * 出块奖励：5个LC(Latte Coin)
     */
    public static final float BLOCK_SUBSIDY = 5;

    /**
     * 每包含一个交易信息则奖励0.01个LC
     */
    public static final float TRANSACTION_SUBSIDY = 0.1f;
}
