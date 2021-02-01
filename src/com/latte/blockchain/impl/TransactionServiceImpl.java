package com.latte.blockchain.impl;

import com.latte.blockchain.entity.LatteCoin;
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

    private LatteCoin latteCoin = LatteCoin.getInstance();

    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        String data = CryptoUtil.getStringFromKey(transaction.getSender()) +
                CryptoUtil.getStringFromKey(transaction.getRecipient()) +
                transaction.getValue();
        transaction.setSignature(CryptoUtil.applySignature(privateKey, data));
    }

    /**
     * 验证交易
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    @Override
    public boolean verifyTransaction(Transaction transaction) {
        if (!isValidSignature(transaction)) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // 收集交易输入
        ArrayList<TransactionInput> inputs = transaction.getInputs();
        for (TransactionInput input : inputs) {
            input.setUTXO(latteCoin.getUTXOs().get(input.getTransactionOutputId()));
        }

        // 检查交易是否有效
        float inputsValue = getInputsValue(inputs);
        if (inputsValue < latteCoin.getMinimumTransaction()) {
            System.out.println("Transaction Inputs too small: " + getInputsValue(inputs));
            return false;
        }

        // 计算剩余价值
        float leftOver = inputsValue - transaction.getValue();
        transaction.setId(calculateTransactionHash(transaction));

        // 将金额发送至接收方
        transaction.getOutputs().add(
                new TransactionOutput(transaction.getRecipient(),
                transaction.getValue(),
                transaction.getId())
        );

        // 将剩余金额返回至发送方
        transaction.getOutputs().add(
                new TransactionOutput(transaction.getSender(),
                        leftOver,
                        transaction.getId())
        );
        return true;
    }

    public Float getInputsValue(ArrayList<TransactionInput> inputs) {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.getUTXO() != null) {
                total += i.getUTXO().getValue();
            }
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
                transaction.getSignature()
        );
    }

    @Override
    public String calculateTransactionHash(Transaction transaction) {
        transaction.setSequence(transaction.getSequence() + 1);
        return CryptoUtil.applySha256(
                CryptoUtil.getStringFromKey(transaction.getSender()) +
                        CryptoUtil.getStringFromKey(transaction.getRecipient()) +
                        transaction.getValue() +
                        transaction.getSequence()
        );
    }
}
