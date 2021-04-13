package com.latte.blockchain.entity;

import cn.hutool.crypto.SecureUtil;
import com.latte.blockchain.impl.MineServiceImpl;
import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 钱包类
 *
 * @author float
 * @since 2021/1/27
 */
public class Wallet {

    /**
     * 用户名
     */
    @Getter
    @Setter
    private String name;

    /**
     * 用户私钥信息
     */
    @Getter
    @JsonIgnore
    private final PrivateKey privateKey;

    /**
     * 用户公钥信息
     */
    @Getter
    @JsonIgnore
    private final PublicKey publicKey;

    @Getter
    @JsonIgnore
    private final String publicKeyString;

    /**
     * 管理员群签名打开私钥
     */
    @Setter
    @Getter
    @JsonIgnore
    private AdminGroupOpenKey ok;

    /**
     * 管理员群签名私钥
     */
    @Setter
    @Getter
    @JsonIgnore
    private AdminGroupSecretKey ask;

    @Getter
    @Setter
    @JsonIgnore
    private UserGroupSecretKey gsk;

    /**
     * 用户挖矿线程
     */
    @Getter
    @JsonIgnore
    private final Thread workerThread;

    /**
     * 用户账户金额
     */
    @Getter
    @JsonIgnore
    private HashMap<String, Utxo> UTXOs = new HashMap<>();

    @Setter
    @Getter
    private float balance;

    public Wallet() {
        // 初始化用户的公私钥信息
        KeyPair keyPair = SecureUtil.generateKeyPair("SM2");

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        publicKeyString = CryptoUtil.getStringFromKey(publicKey);
        String publicKeyString = CryptoUtil.getStringFromKey(publicKey);

        name = publicKeyString.substring(94);
        balance = 0;
        workerThread = new Thread(new MineServiceImpl(), name);
    }
}
