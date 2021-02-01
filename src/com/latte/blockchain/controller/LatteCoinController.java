package com.latte.blockchain.controller;

import java.security.Security;

import com.latte.blockchain.entity.LatteCoin;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionOutput;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * @author float311
 * @since 2021/01/31
 */
@RestController
public class LatteCoinController {

    @Autowired
    private IMineService mineService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IWalletService walletService;

    private LatteCoin latteCoin = LatteCoin.getInstance();

    @GetMapping("/init")
    public String initSystem() {
        Security.addProvider(new BouncyCastleProvider());

        // 创建钱包
        Wallet walletA = new Wallet();
        // Wallet walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // 创建币基交易
        Transaction initTransaction = new Transaction(
                coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        // 进行签名
        transactionService.generateSignature(coinbase.getPrivateKey(), initTransaction);
        // 设置交易id
        initTransaction.setId("0");
        // 添加交易输出
        initTransaction.getOutputs().add(
                new TransactionOutput(
                        initTransaction.getRecipient(),
                        initTransaction.getValue(),
                        initTransaction.getId()));
        // 将交易储存进UTXOs
        latteCoin.getUTXOs().put(initTransaction.getOutputs().get(0).getId(), initTransaction.getOutputs().get(0));

        System.out.println("Starting mining block");
        return latteCoin.toString();
    }

}
