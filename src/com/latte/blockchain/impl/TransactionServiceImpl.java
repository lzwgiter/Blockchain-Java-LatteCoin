package com.latte.blockchain.impl;

import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.service.ITransactionPoolService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.CryptoUtil;

import com.latte.blockchain.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/01/29
 */
@Service
public class TransactionServiceImpl implements ITransactionService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IWalletService walletService;

    @Autowired
    private ITransactionPoolService transactionPoolService;

    /**
     * 发起一笔交易
     *
     * @param sender    交易发起方
     * @param recipient 交易接受方
     * @param value     交易金额
     * @return String 交易信息
     */
    @Override
    public String createTransaction(String sender, String recipient, float value) {
        // 处理网络原因导致的字符问题
        sender = sender.replace(" ", "+");
        recipient = recipient.replace(" ", "+");
        Transaction newTransaction = walletService.sendFunds(latteChain.getUsers().get(sender),
                latteChain.getUsers().get(recipient), value);
        // 若交易建立成功，则将交易放入交易池
        if (newTransaction != null) {
            transactionPoolService.addTransaction(newTransaction);
            return "交易成功建立！交易信息:\n" + JsonUtil.toJson(newTransaction);
        } else {
            return "账户[" + sender + "]余额不足，交易失败！";
        }
    }

    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        String data = transaction.getSenderString() +
                transaction.getRecipientString() +
                transaction.getValue();
        transaction.setSignature(CryptoUtil.applySignature(privateKey, data));
    }

    /**
     * 进行交易
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    @Override
    public boolean processTransaction(Transaction transaction) {
        // 首先检查一个交易的合法性
        // 验签
        if (!isValidSignature(transaction)) {
            System.out.println("# 交易签名验证失败");
            return false;
        }

        for (TransactionInput input : transaction.getInputs()) {
            if (latteChain.getUTXOs().containsKey(input.getTransactionOutputId())) {
                input.setUTXO(latteChain.getUTXOs().get(input.getTransactionOutputId()));
            } else {
                // 当前交易已经被消耗，挖矿失败，退出
                return false;
            }
        }

        // 检查交易输入是否符合最低交易金额TODO: 应该检查这笔交易的交易金额而不是输入的金额！
        float inputsValue = getInputsValue(transaction);
        float minimum = latteChain.getMinimumTransactionValue();
        if (inputsValue - transaction.getValue() < minimum) {
            System.out.println("交易金额过小");
            return false;
        }
        // 扣除交易方的UTXO
        for (TransactionInput input : transaction.getInputs()) {
            latteChain.getUTXOs().remove(input.getTransactionOutputId());
        }

        // 计算剩余价值
        float leftOver = inputsValue - transaction.getValue();
        // 设置交易ID
        transaction.setId(calculateTransactionHash(transaction));

        // 将金额发送至接收方
        transaction.getOutputs().add(new TransactionOutput(transaction.getRecipient(), transaction.getValue()));

        // 将剩余金额返回至发送方
        transaction.getOutputs().add(new TransactionOutput(transaction.getSender(), leftOver));
        return true;
    }

    /**
     * 获取交易输入的总值
     *
     * @param transaction 交易
     * @return 输入总值
     */
    @Override
    public float getInputsValue(Transaction transaction) {
        float total = 0;
        for (TransactionInput input : transaction.getInputs()) {
            if (input.getUTXO() != null) {
                total += input.getUTXO().getValue();
            }
        }
        return total;
    }

    /**
     * 获取交易输出的总值
     *
     * @param transaction 交易
     * @return 输出总值
     */
    @Override
    public float getOutputsValue(Transaction transaction) {
        float total = 0;
        for (TransactionOutput output : transaction.getOutputs()) {
            total += output.getValue();
        }
        return total;
    }

    /**
     * 检查是否是有效的交易
     *
     * @param transaction {@link Transaction} 交易信息
     * @return 是则返回true
     */
    @Override
    public boolean isValidTransaction(Transaction transaction) {
        // 检查当前交易的签名
        if (!isValidSignature(transaction)) {
            System.out.println("# Signature on Transaction(" + transaction.getId() + ") is Invalid");
            return false;
        }

        // 检查输入输出的一致性
        if (getInputsValue(transaction) != getOutputsValue(transaction)) {
            System.out.println("# Inputs are note equal to outputs on Transaction(" + transaction.getId() + ")");
            return false;
        }
        return true;
    }

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    @Override
    public boolean isValidSignature(Transaction transaction) {
        return CryptoUtil.verifySignature(transaction.getSender(),
                transaction.getData(),
                transaction.getSignature());
    }

    @Override
    public String calculateTransactionHash(Transaction transaction) {
        transaction.setSequence(transaction.getSequence() + 1);
        return CryptoUtil.applySha256(
                transaction.getSenderString() +
                        transaction.getRecipientString() +
                        transaction.getValue() +
                        transaction.getSequence());
    }

}