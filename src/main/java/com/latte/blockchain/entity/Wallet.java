package com.latte.blockchain.entity;

import cn.hutool.crypto.SecureUtil;
import com.latte.blockchain.impl.MineServiceImpl;
import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.util.HashMap;

import lombok.Getter;
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
    private final String name;

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
     * 用户挖矿线程
     */
    @Getter
    @JsonIgnore
    private final Thread workerThread;

    @Getter
    private HashMap<String, Utxo> UTXOs = new HashMap<>();

    public Wallet() {
        // 初始化用户的公私钥信息
        KeyPair keyPair = SecureUtil.generateKeyPair("SM2");

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        publicKeyString = CryptoUtil.getStringFromKey(publicKey);
        String publicKeyString = CryptoUtil.getStringFromKey(publicKey);

            name = publicKeyString.substring(94);
        workerThread = new Thread(new MineServiceImpl(), name);
    }
}
