package com.latte.blockchain.impl;

import com.latte.blockchain.dao.UtxoDao;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author float311
 * @since 2021/01/31
 */
@Service
public class WalletServiceImpl implements IWalletService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private ITransactionService transactionService;

    /**
     * UTXO DAO
     */
    @Autowired
    private UtxoDao utxoDao;

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
        for (TransactionOutput record : utxoDao.findAll()) {
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
     * @param address 用户账户地址
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
        if (this.getBalance(senderWallet) < value) {
            // 发起方余额不足，取消交易
            return null;
        }

        // 开始构造交易输入
        Set<String> inputs = new HashSet<>();
        float total = 0;

        // 收集交易发起者的UTXO
        for (TransactionOutput item : senderWallet.getUTXOs().values()) {
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
        // 计算交易ID
        newTransaction.setId(transactionService.calculateTransactionHash(newTransaction));
        transactionService.generateSignature(senderWallet.getPrivateKey(), newTransaction);


        // 扣除发起者的花费的UTXO(从个人钱包里) TODO: 需要从个人钱包扣除吗？还是直接从全局扣除？需要个人钱包这个概念吗？
        for (String input : inputs) {
            senderWallet.getUTXOs().remove(input);
        }
        return newTransaction;
    }
}
