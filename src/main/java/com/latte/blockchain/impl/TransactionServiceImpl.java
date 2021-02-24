package com.latte.blockchain.impl;

import com.latte.blockchain.dao.TransactionDao;
import com.latte.blockchain.dao.UtxoDao;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.CryptoUtil;

import com.latte.blockchain.utils.JsonUtil;
import com.latte.blockchain.utils.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author float311
 * @since 2021/01/29
 */
@Service
public class TransactionServiceImpl implements ITransactionService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IWalletService walletService;

    /**
     * 交易DAO对象
     */
    @Autowired
    private TransactionDao transactionDao;

    /**
     * UTXO DAO对象
     */
    @Autowired
    private UtxoDao utxoDao;

    /**
     * 锁对象
     */
    final Lock lock = Lock.getLock();

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
        Transaction newTransaction = walletService.sendFunds(sender, recipient, value);
        // 若交易建立成功，则将交易放入交易池
        if (newTransaction != null) {
            transactionDao.save(newTransaction);
            synchronized (lock) {
                lock.notifyAll();
            }
            return JsonUtil.toJson(newTransaction);
        } else {
            return "账户[" + sender + "]余额不足，交易失败！";
        }
    }

    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        transaction.setSignature(CryptoUtil.applySignature(privateKey, transaction.getData()));
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
        // 验签 TODO： 交易验签的操作会耗费时间，导致线程阻塞
//        if (!isValidSignature(transaction)) {
//            System.out.println("# 交易签名验证失败");
//            return false;
//        }

        // 检查所需UTXO是否已经被消耗
        for (String input : transaction.getInputs()) {
            if (!utxoDao.existsById(input)) {
                // 当前交易已经被消耗，挖矿失败，退出
                return false;
            }
        }

        // 检查交易输入是否符合最低交易金额
        float inputsValue = getInputsValue(transaction);
        float minimum = latteChain.getMinimumTransactionValue();
        if (transaction.getValue() < minimum) {
            System.out.println("交易金额过小");
            return false;
        }

        // 从全局删除交易方的UTXO
        for (String input : transaction.getInputs()) {
            utxoDao.deleteById(input);
        }

        // 计算剩余价值
        float leftOver = inputsValue - transaction.getValue();

        PublicKey senderAddress = latteChain.getUsers().get(transaction.getSenderString()).getPublicKey();
        PublicKey recipientAddress = latteChain.getUsers().get(transaction.getRecipientString()).getPublicKey();


        // 添加交易输出
        // 将金额发送至接收方
        transaction.getOutputs().add(new TransactionOutput(recipientAddress, transaction.getValue()));

        // 将剩余金额返回至发送方
        transaction.getOutputs().add(new TransactionOutput(senderAddress, leftOver));
        return true;
    }

    /**
     * 获取交易输入的总值
     *
     * @param transaction 待计算交易
     * @return 输入总值
     */
    @Override
    public float getInputsValue(Transaction transaction) {
        float total = 0;
        TransactionOutput output;
        for (String input : transaction.getInputs()) {
            output = utxoDao.getTransactionOutputById(input);
            total += output.getValue();
        }
        return total;
    }

    /**
     * 获取交易输出的总值
     *
     * @param transaction 待计算交易
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
        transaction.setSender(latteChain.getUsers().get(transaction.getSenderString()).getPublicKey());
        if (!isValidSignature(transaction)) {
            System.out.println("# Signature on Transaction(" + transaction.getId() + ") is Invalid");
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
        return CryptoUtil.applySha256(transaction.getData());
    }

}