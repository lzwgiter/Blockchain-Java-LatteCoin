package com.latte.blockchain.impl;

import com.latte.blockchain.repository.UtxoRepo;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.IGsService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;

import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.JsonUtil;
import com.latte.blockchain.utils.LatteChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author float311
 * @since 2021/01/31
 */
@Service
public class WalletServiceImpl implements IWalletService {

    private final LatteChain latteChain = LatteChain.getInstance();

//    @Autowired
//    private ITransactionService transactionService;

    @Autowired
    private IGsService iGsService;

    /**
     * UTXO DAO
     */
    @Autowired
    private UtxoRepo utxoDao;

    /**
     * 获取账户余额
     *
     * @param userWallet {@link Wallet}用户钱包
     * @return 账户余额
     */
    @Override
    public float getBalance(Wallet userWallet) {
        float total = 0;
        // 从全局的UTXO中收集该用户的UTXO并进行结算
        for (Utxo record : utxoDao.findAll()) {
            if (record.getRecipientString().equals(userWallet.getPublicKeyString())) {
                userWallet.getUTXOs().put(record.getId(), record);
                total += record.getValue();
            }
        }
        return total;
    }

    /**
     * 获取账户余额
     *
     * @param address String 用户账户地址
     * @return 账户余额
     */
    @Override
    public float getBalance(String address) {
        address = address.replace(" ", "+");
        Wallet userWallet = latteChain.getUsers().get(address);
        return getBalance(userWallet);
    }

    /**
     * 向recipient发起一笔值为value的交易
     *
     * @param sender    发送方
     * @param recipient 接收方
     * @param value     交易值
     * @return {@link Transaction} 交易
     */
    @Override
    public Transaction sendFunds(String sender, String recipient, float value) {
        Wallet senderWallet = latteChain.getUsers().get(sender);
        Wallet recipientWallet = latteChain.getUsers().get(recipient);
        if (senderWallet == null || recipientWallet == null) {
            // 请求用户不存在
            return null;
        }

        // 收集用户的账户金额
        if (this.getBalance(senderWallet) < value) {
            // 发起方余额不足，取消交易
            return null;
        }

        // 开始构造交易输入
        Set<String> inputs = new HashSet<>();
        float total = 0;

        // 收集交易发起者的UTXO
        for (Utxo item : senderWallet.getUTXOs().values()) {
            total += item.getValue();
            inputs.add(item.getId());
            // 已经满足支出需求
            if (total >= value) {
                break;
            }
        }
        // 构造新交易
        Transaction newTransaction = new Transaction(senderWallet.getPublicKey(),
                recipientWallet.getPublicKey(), value, inputs);
        newTransaction.setSenderString(sender);
        newTransaction.setRecipientString(recipient);
        // 设置交易数据
        newTransaction.setData(CryptoUtil.getEncryptedTransaction(newTransaction,
                LatteChain.getInstance().getAdminPublicKey()));

        // transactionService.generateSignature(senderWallet.getPrivateKey(), newTransaction);

        // TODO: 对交易进行群签名
        GroupSignature signature = iGsService.gSign(newTransaction.getData(), senderWallet.getGsk());
        newTransaction.setSignature(JsonUtil.toJson(signature).getBytes(StandardCharsets.UTF_8));

        // 扣除发起者的花费的UTXO(从个人钱包里)
        for (String input : inputs) {
            senderWallet.getUTXOs().remove(input);
        }
        return newTransaction;
    }
}
