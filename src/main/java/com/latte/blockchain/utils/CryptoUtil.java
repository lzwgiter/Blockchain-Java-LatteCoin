package com.latte.blockchain.utils;

import cn.hutool.crypto.asymmetric.KeyType;
import com.latte.blockchain.entity.Transaction;

import java.util.List;
import java.util.Base64;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import cn.hutool.crypto.SmUtil;

/**
 * 生成电子签名的工具类
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
     * SM2签名函数
     *
     * @param privateKey 私钥
     * @param msg        消息
     * @return byte[] 签名信息
     */
    public static byte[] applySm2Signature(PrivateKey privateKey, String msg) {
        return SmUtil.sm2(privateKey, null).sign(msg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SM2签名验证函数
     *
     * @param publicKey 公钥
     * @param msg       消息
     * @param signature 签名
     * @return 是否为一个合法的SM2签名信息
     */
    public static boolean verifySm2Signature(PublicKey publicKey, String msg, byte[] signature) {
        return SmUtil.sm2(null, publicKey).verify(msg.getBytes(StandardCharsets.UTF_8), signature);
    }

    /**
     * SM2加密函数
     *
     * @param publicKey 加密公钥
     * @param msg       待加密消息
     * @return String 加密结果
     */
    public static String applySm2Encrypt(PublicKey publicKey, String msg) {
        // return SmUtil.sm2(null, publicKey).encryptBcd(msg, KeyType.PublicKey);
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
        String originData = transaction.getSenderString() + '-' +
                transaction.getRecipientString() + '-' +
                transaction.getValue() + '-' +
                transaction.getTimeStamp();
        return applySm2Encrypt(adminPubKey, originData);
    }

    /**
     * 根据加密信息解密交易数据
     *
     * @param input 加密信息
     * @return 交易信息 {@link Transaction}
     */
    public static String getDecryptedTransaction(String input, PrivateKey adminPriKey) {
        String originData = applySm2Decrypted(adminPriKey, input);
        String[] raw = originData.split("-");
        return "Sender: " + raw[0] + "; Recipient: " + raw[1] +
                "; Value: " + raw[2] + "; TimeStamp: " + raw[3];
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