package com.latte.blockchain.impl;

import com.latte.blockchain.entity.LatteCoin;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;
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

    private LatteCoin latteCoin = LatteCoin.getInstance();

    @Autowired
    private ITransactionService transactionService;

    /**
     * 获取账户余额
     *
     * @param publicKey 用户地址
     * @return Float
     */
    @Override
    public Float getBalance(PublicKey publicKey) {
        float total = 0;
        HashMap<String, TransactionOutput> UTXOs = latteCoin.getUTXOs();
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isbelongto(publicKey)) {
                UTXOs.put(UTXO.getId(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    /**
     * 向recipient发起一笔值为value的交易
     *
     * @param sender           发送方
     * @param senderPrivateKey 签名秘钥
     * @param recipient        接收方
     * @param value            交易值
     * @return
     */
    @Override
    public Transaction sendFunds(PublicKey sender, PrivateKey senderPrivateKey, PublicKey recipient, Float value) {
        if (this.getBalance(sender) < value) {
            System.out.println("#余额不足. 交易取消");
            return null;
        }
        // 交易输入
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        HashMap<String, TransactionOutput> UTXOs = this.latteCoin.getUTXOs();

        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput utxo = item.getValue();
            total += utxo.getValue();
            inputs.add(new TransactionInput(utxo.getId()));
            // 已经满足支出需求
            if (total > value) {
                break;
            }
        }
        // 构造新交易
        Transaction newTransaction = new Transaction(sender, recipient, value, inputs);
        transactionService.generateSignature(senderPrivateKey, newTransaction);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }
}
