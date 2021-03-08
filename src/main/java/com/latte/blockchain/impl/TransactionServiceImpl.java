package com.latte.blockchain.impl;

import com.latte.blockchain.dao.TransactionDao;
import com.latte.blockchain.dao.TransactionPoolDao;
import com.latte.blockchain.dao.UtxoDao;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.CryptoUtil;

import com.latte.blockchain.utils.JsonUtil;
import com.latte.blockchain.utils.LatteChain;
import com.latte.blockchain.utils.LockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
     * 交易池DAO对象
     */
    @Autowired
    private TransactionPoolDao transactionPoolDao;

    /**
     * UTXO DAO对象
     */
    @Autowired
    private UtxoDao utxoDao;

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
        ReentrantLock requestLock = LockUtil.getLockUtil().getRequestLock();
        Condition condition = LockUtil.getLockUtil().getCondition();
        // 若交易建立成功，则将交易放入交易池
        if (newTransaction != null) {
            requestLock.lock();
            try {
                transactionDao.save(newTransaction);
                transactionPoolDao.save(
                        new TransactionsPoolEntity(newTransaction.getId(), newTransaction.getTimeStamp()));
                condition.signalAll();
            } finally {
                requestLock.unlock();
            }
            return JsonUtil.toJson(newTransaction);
        } else {
            return "账户[" + sender + "]余额不足，交易失败！";
        }
    }

    /**
     * 计算交易输出
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    @Override
    public synchronized boolean processTransaction(Transaction transaction) {
        // 首先检查一个交易的合法性
        // 验签 TODO： 交易验签的操作会耗费时间，导致线程阻塞
//        if (!isValidSignature(transaction)) {
//            System.out.println("# 交易签名验证失败");
//            return false;
//        }
        float inputsValue = getInputsValue(transaction);
        // 计算剩余价值
        if (inputsValue == 0) {
            // 输入已经被消耗
            return false;
        }

        // 从全局删除交易方的UTXO
        for (String input : transaction.getInputUtxos()) {
            utxoDao.deleteById(input);
        }

        float leftOver = inputsValue - transaction.getValue();

        PublicKey senderAddress = latteChain.getUsers().get(transaction.getSenderString()).getPublicKey();
        PublicKey recipientAddress = latteChain.getUsers().get(transaction.getRecipientString()).getPublicKey();


        // 添加交易输出
        // 将金额发送至接收方
        transaction.getOutputUtxos().add(new Utxo(recipientAddress, transaction.getValue()));

        // 将剩余金额返回至发送方
        transaction.getOutputUtxos().add(new Utxo(senderAddress, leftOver));
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
        Utxo output;
        ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();
        lock.readLock().lock();
        try {
            for (String input : transaction.getInputUtxos()) {
                output = utxoDao.getTransactionOutputById(input);
                if (output == null) {
                    return 0;
                } else {
                    total += output.getValue();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return total;
    }

    /**
     * 获取targetUserName作为接受方的所有交易的交易链
     *
     * @param targetUserName 待审计用户名称
     * @return 交易链信息
     */
    @Override
    public String auditTransaction(String targetUserName) {
        return null;
    }

    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        transaction.setSignature(CryptoUtil.applySm2Signature(privateKey, transaction.getData()));
    }

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    @Override
    public boolean isValidSignature(Transaction transaction) {
        return CryptoUtil.verifySm2Signature(transaction.getSender(),
                transaction.getData(),
                transaction.getSignature());
    }

    @Override
    public String calculateTransactionHash(Transaction transaction) {
        return CryptoUtil.applySm3Hash(transaction.getData());
    }

}