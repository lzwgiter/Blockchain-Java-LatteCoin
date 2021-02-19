package com.latte.blockchain.entity;

import com.latte.blockchain.impl.MineServiceImpl;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.JsonUtil;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;

import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
    @JsonSerialize(using = JsonUtil.class)
    private final PublicKey publicKey;

    /**
     * 用户挖矿线程
     */
    @Getter
    @JsonIgnore
    private final Thread workerThread;

    @Getter
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet() {
        // 初始化用户的公私钥信息
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            String publicKeyString = CryptoUtil.getStringFromKey(publicKey);
            name = publicKeyString.substring(94);
            workerThread = new Thread(new MineServiceImpl(), name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}