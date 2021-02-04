package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionOutput;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.IChainService;
import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.CryptoUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 挖矿服务类
 *
 * @author float311
 * @since 2021/01/28
 */
@Service
public class MineServiceImpl implements IMineService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IChainService chainService;

    @Autowired
    private ITransactionService transactionService;

    /**
     * 计算新的区块哈希值并计算默克根
     *
     * @param block 前一区块的Hash值
     * @return boolean true - 成功挖掘出区块并成功添加到了区块链中
     */
    @Override
    public boolean mineNewBlock(Block block) {
        String difficultyString = CryptoUtil.getDifficultyString();
        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot(block.getTransactions()));
        String hash = this.calculateBlockHash(block);
        while (!hash.substring(0, LatteChain.getDifficulty()).equals(difficultyString)) {
            block.setNonce(block.getNonce() + 1);
            hash = this.calculateBlockHash(block);
        }
        block.setHash(hash);
        System.out.println("[Mined √] : " + hash);
        if (chainService.addBlock(block)) {
            System.out.println("[Block √] : 区块[ " + block.getHash() + " ]已添加到系统区块链中！");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 执行交易并添加到区块中
     *
     * @param block       区块
     * @param transaction 交易信息
     */
    @Override
    public boolean addTransaction(Block block, Transaction transaction) {
        // 交易为空，不合法
        if (transaction == null) {
            return false;
        }

        // 当前区块所能包含交易数量已达上限
        if (block.getTransactions().size() == LatteChainEnum.MAX_TRANSACTION_AMOUNT) {
            return false;
        }

        if (!block.getPreviousHash().equals(LatteChainEnum.ZERO_HASH)) {
            // 非初始块则检查交易合法性(输入输出是否匹配、签名是否正确)并执行一笔交易
            if (!transactionService.processTransaction(transaction)) {
                System.out.println("交易执行失败");
                return false;
            }
        }

        // 将交易添加至区块中
        block.getTransactions().add(transaction);
        // 将当前交易记在系统全局UTXO中
        for (TransactionOutput output : transaction.getOutputs()) {
            latteChain.getUTXOs().put(output.getId(), output);
        }
        System.out.println("交易执行成功！");
        return true;
    }

    /**
     * 计算得到合适的哈希值
     *
     * @param block 区块
     * @return String 哈希值
     */
    @Override
    public String calculateBlockHash(Block block) {
        return CryptoUtil.applySha256(
                block.getId() +
                        block.getPreviousHash() +
                        block.getTimeStamp() +
                        block.getMerkleRoot() +
                        block.getNonce());
    }
}
