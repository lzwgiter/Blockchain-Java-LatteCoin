package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.impl.TransactionServiceImpl;

import java.security.PrivateKey;
import java.security.PublicKey;

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
     * @param publicKey 用户地址
     * @return Float
     */
    Float getBalance(PublicKey publicKey);

    /**
     * 向recipient发起一笔值为value的交易
     *
     * @param sender           发送方
     * @param senderPrivateKey 签名秘钥
     * @param recipient        接收方
     * @param value            交易值
     * @return {@link Transaction}
     */
    Transaction sendFunds(PublicKey sender, PrivateKey senderPrivateKey, PublicKey recipient, Float value);
}
