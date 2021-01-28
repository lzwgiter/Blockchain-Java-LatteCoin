package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.service.IMine;
import com.latte.blockchain.utils.CryptoUtil;

import java.util.ArrayList;

/**
 * 挖矿服务类
 *
 * @author float311
 * @since 2021/01/28
 */
public class MineImpl implements IMine {

    private Block newBlock;

    @Override
    public Block mineNewBlock(String preHash, int difficulty, ArrayList<Transaction> transactions) {
        this.newBlock = new Block(preHash);
        String difficultyString = CryptoUtil.getDifficultyString(difficulty);
        String hash = calculateHash();
        while (!hash.substring(0, difficulty).equals(difficultyString)) {
            this.newBlock.setNonce(this.newBlock.getNonce() + 1);
            hash = calculateHash();
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

    }


    @Override
    public String calculateHash() {
        return CryptoUtil.applySha256(
                this.newBlock.getId() +
                        this.newBlock.getPreviousHash() +
                        this.newBlock.getTimeStamp() +
                        this.newBlock.getMerkleRoot() +
                        this.newBlock.getNonce()
                );
    }
}
