package com.latte.blockchain.impl;

import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.utils.CryptoUtil;

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

        // 设置交易输入
        ArrayList<TransactionInput> inputs = transaction.getInputs();
        for (TransactionInput input : inputs) {
            input.setUTXO(latteChain.getUTXOs().get(input.getTransactionOutputId()));
        }

        // 检查交易输入是否符合最低交易金额
        float inputsValue = getInputsValue(transaction);
        float minimum = latteChain.getMinimumTransactionValue();
        if (inputsValue < minimum) {
            System.out.println("Transaction Inputs too small: current inputs value is " +
                    inputsValue +
                    "while minimum value" + minimum + " is expected");
            return false;
        }

        // 计算剩余价值
        float leftOver = inputsValue - transaction.getValue();
        // 设置交易ID
        transaction.setId(calculateTransactionHash(transaction));

        // 将金额发送至接收方
        transaction.getOutputs().add(
                new TransactionOutput(transaction.getRecipient(), transaction.getValue()));

        // 将剩余金额返回至发送方
        transaction.getOutputs().add(
                new TransactionOutput(transaction.getSender(), leftOver));
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