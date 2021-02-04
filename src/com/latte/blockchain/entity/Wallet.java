package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.latte.blockchain.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;

/**
 * 钱包类
 *
 * @author float
 * @since 2021/1/27
 */
public class Wallet {

    @Getter
    @JsonIgnore
    private final PrivateKey privateKey;

    @Getter
    @JsonSerialize(using = JsonUtil.class)
    private final PublicKey publicKey;

    @Getter
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
