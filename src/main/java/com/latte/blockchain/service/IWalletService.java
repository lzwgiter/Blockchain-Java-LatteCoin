package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.Wallet;

/**
 * 钱包服务接口类
 *
 * @author float311
 * @since 2021/01/31
 */
public interface IWalletService {
    /**
     * 获取账余额
     *
     * @param wallet {@link Wallet} 用户钱包
     * @return float
     */
    float getBalance(Wallet wallet);

    /**
     * 获取账余额
     *
     * @param address 用户钱包地址
     * @return float
     */
    float getBalance(String address);

    /**
     * 向recipient发起一笔值为value的交易
     *
     * @param sender    {@link Wallet} 发送方
     * @param recipient {@link Wallet} 接收方
     * @param value     交易值
     * @return {@link Transaction} 交易
     */
    Transaction sendFunds(Wallet sender, Wallet recipient, float value);
}
