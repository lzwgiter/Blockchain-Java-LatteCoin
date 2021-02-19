package com.latte.blockchain.utils;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.enums.LatteChainEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 生成电子签名的工具类
 *
 * @author float
 * @since 2021/1/27
 */
public class CryptoUtil {

    /**
     * sha256哈希函数
     *
     * @param msg 待哈希消息
     * @return String 哈希值
     */
    public static String applySha256(String msg) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ECDSA签名函数
     *
     * @param privateKey 私钥
     * @param msg        消息
     * @return byte[] {@link Signature}
     */
    public static byte[] applySignature(PrivateKey privateKey, String msg) {
        Signature dsa;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] msgBytes = msg.getBytes();
            dsa.update(msgBytes);
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ECDSA签名验证函数
     *
     * @param publicKey 公钥
     * @param msg       消息
     * @param signature 签名
     * @return boolean
     */
    public static boolean verifySignature(PublicKey publicKey, String msg, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(msg.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造指定难度的0填充字符串
     *
     * @return String
     */
    public static String getDifficultyString() {
        return new String(new char[LatteChainEnum.DIFFICULTY]).replace('\0', '0');
    }

    /**
     * Base64加密
     *
     * @param key {@link Key}
     * @return String
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 计算Merkle根值
     *
     * @param transactions 交易@{@link Transaction}
     * @return String
     */
    public static String calculateMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getId());
        }
        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<>();
            // 每次选择两个交易的散列值重新计算
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}