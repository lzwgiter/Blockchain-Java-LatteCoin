package com.latte.blockchain.impl;

import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
     * 获取账户余额
     *
     * @param userWallet 用户地址
     * @return Float
     */
    @Override
    public float getBalance(Wallet userWallet) {
        float total = 0;
        // 从全局的UTXO中收集该用户的UTXO并进行结算
        for (Map.Entry<String, TransactionOutput> item : latteChain.getUTXOs().entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isBelongTo(userWallet.getPublicKey())) {
                userWallet.getUTXOs().put(UTXO.getId(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    @Override
    public float getBalance(String address) {
        address = address.replace(" ", "+");
        Wallet userWallet = latteChain.getUsers().get(address);
        return getBalance(userWallet);
    }

    /**
     * 向recipient发起一笔值为value的交易
     *
     * @param sender    {@link Wallet} 发送方
     * @param recipient {@link Wallet} 接收方
     * @param value     交易值
     * @return {@link Transaction} 交易
     */
    @Override
    public Transaction sendFunds(Wallet sender, Wallet recipient, float value) {
        if (this.getBalance(sender) < value) {
            // 发起方余额不足，取消交易
            System.out.println("# 余额不足. 交易取消");
            return null;
        }

        // 开始构造交易输入
        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        TransactionOutput utxo;

        // 收集交易发起者的所有UTXO
        for (Map.Entry<String, TransactionOutput> item : sender.getUTXOs().entrySet()) {
            utxo = item.getValue();
            total += utxo.getValue();
            inputs.add(new TransactionInput(utxo.getId()));
            // 已经满足支出需求
            if (total >= value) {
                break;
            }
        }
        // 构造新交易
        Transaction newTransaction = new Transaction(sender.getPublicKey(), recipient.getPublicKey(), value, inputs);
        // 计算交易ID
        newTransaction.setId(transactionService.calculateTransactionHash(newTransaction));
        transactionService.generateSignature(sender.getPrivateKey(), newTransaction);


        // 扣除发起者的花费的UTXO(从全局和个人钱包里)
        for (TransactionInput input : inputs) {
            sender.getUTXOs().remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }
}
