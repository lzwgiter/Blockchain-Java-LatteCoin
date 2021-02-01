package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.enums.HashEnum;
import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.utils.CryptoUtil;

import org.omg.IOP.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 挖矿服务类
 *
 * @author float311
 * @since 2021/01/28
 */
@Service
public class MineServiceImpl implements IMineService {

    private Block newBlock = null;

    @Autowired
    private ITransactionService transactionService;

    // TODO: preHash导致不能自动注入
    /**
    public MineServiceImpl(String preHash) {
        this.newBlock = new Block(preHash);
    }*/

    /**
     * 挖新的区块
     *
     * @param difficulty   难度值
     * @param transactions 交易信息
     * @return Block {@link Block}
     */
    @Override
    public Block mineNewBlock(int difficulty, ArrayList<Transaction> transactions) {
        String difficultyString = CryptoUtil.getDifficultyString(difficulty);
        String hash = calculateBlockHash();
        while (!hash.substring(0, difficulty).equals(difficultyString)) {
            this.newBlock.setNonce(this.newBlock.getNonce() + 1);
            hash = calculateBlockHash();
        }
        this.newBlock.setHash(hash);
        this.newBlock.setMerkleRoot(CryptoUtil.getMerkleRoot(transactions));
        System.out.println("[Mined √] : " + hash);
        return this.newBlock;
    }

    /**
     * 添加交易信息
     *
     * @param transaction 交易信息
     */
    @Override
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        if (!this.newBlock.getPreviousHash().equals(HashEnum.ZEROHASH)) {
            // 非初始块
            if (!transactionService.verifyTransaction(transaction)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        this.newBlock.getTransactions().add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }


    /**
     * 计算得到合适的哈希值
     *
     * @return String
     */
    @Override
    public String calculateBlockHash() {
        return CryptoUtil.applySha256(
                this.newBlock.getId() +
                        this.newBlock.getPreviousHash() +
                        this.newBlock.getTimeStamp() +
                        this.newBlock.getMerkleRoot() +
                        this.newBlock.getNonce()
        );
    }
}
