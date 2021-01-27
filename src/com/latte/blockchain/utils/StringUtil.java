package com.latte.blockchain.utils;

import com.latte.blockchain.Transaction;

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
public class StringUtil {

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
     * 椭圆曲线签名算法
     *
     * @param privateKey 私钥
     * @param msg        消息
     * @return byte[] signature
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
     * 验证签名
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
     * 获取json格式数据
     *
     * @param o java对象
     * @return String
     */
    public static String getJson(Object o) {
        return JsonUtil.toJson(o);
    }

    /**
     * 构造指定难度的0填充字符串
     *
     * @param difficulty 难度
     * @return String
     */
    public static String getDifficultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

    public static void main(String[] args) {
        System.out.println(applySha256("123"));
    }
}
