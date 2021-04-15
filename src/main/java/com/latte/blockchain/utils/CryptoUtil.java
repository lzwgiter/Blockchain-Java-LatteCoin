package com.latte.blockchain.utils;

import cn.hutool.crypto.asymmetric.KeyType;
import com.latte.blockchain.entity.Transaction;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Base64;
import java.util.ArrayList;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import cn.hutool.crypto.SmUtil;
import com.latte.blockchain.entity.TransactionDigest;

/**
 * 密码学具类
 * 提供了SM3哈希算法接口、SM2非对称加密算法接口、密钥与字符串转换函数、区块默克根计算函数
 *
 * @author float
 * @since 2021/1/27
 */
public class CryptoUtil {
    /**
     * Sm3哈希函数
     *
     * @param msg 待哈希消息
     * @return String 哈希值
     */
    public static String applySm3Hash(String msg) {
        return SmUtil.sm3(msg);
    }

    /**
     * SM2加密函数
     *
     * @param publicKey 加密公钥
     * @param msg       待加密消息
     * @return String 加密结果
     */
    public static String applySm2Encrypt(PublicKey publicKey, String msg) {
        return SmUtil.sm2(null, publicKey).encryptBase64(msg, KeyType.PublicKey);
    }

    /**
     * SM2解密函数
     *
     * @param privateKey   解密私钥
     * @param encryptedMsg 待解密消息
     * @return String 解密结果
     */
    public static String applySm2Decrypted(PrivateKey privateKey, String encryptedMsg) {
        return SmUtil.sm2(privateKey, null).decryptStr(encryptedMsg, KeyType.PrivateKey);
    }

    /**
     * 根据交易信息生成加密数据
     *
     * @param adminPubKey 管理员公钥
     */
    public static String getEncryptedTransaction(Transaction transaction, PublicKey adminPubKey) {
        StringBuilder sb = new StringBuilder();
        String originData = transaction.getRecipientString() + '-' +
                transaction.getValue() + '-' +
                transaction.getTimeStamp() + '-';
        sb.append(originData);
        // 计算并设置交易的ID
        transaction.setId(applySm3Hash(originData));
        sb.append(transaction.getId());
        return applySm2Encrypt(adminPubKey, sb.toString());
    }

    /**
     * 根据加密信息解密交易数据
     *
     * @param input 加密信息
     * @return 交易信息 {@link Transaction}
     */
    public static TransactionDigest getDecryptedTransaction(String input, PrivateKey adminPriKey) {
        String originData = applySm2Decrypted(adminPriKey, input);
        String[] raw = originData.split("-");
        return new TransactionDigest(raw[3], raw[0], raw[1], raw[2]);
    }

    /**
     * Base64加密
     *
     * @param key {@link Key}
     * @return Base64加密结果
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
                treeLayer.add(applySm3Hash(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}