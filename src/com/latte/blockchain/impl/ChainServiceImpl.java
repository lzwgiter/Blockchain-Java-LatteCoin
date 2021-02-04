package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;

import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.IUserService;
import com.latte.blockchain.service.IChainService;
import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/02/03
 */
@Service
public class ChainServiceImpl implements IChainService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IUserService userService;

    @Autowired
    private IMineService mineService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IWalletService walletService;

    @Override
    public boolean initChain() {
        Security.addProvider(new BouncyCastleProvider());

        String coinbaseAddress = userService.initUser();
        Wallet coinbaseWallet = latteChain.getUsers().get(coinbaseAddress);
        PublicKey coinbasePublicKey = userService.getUserPublicKey(coinbaseAddress);
        // coinbase交易
        Transaction genesisTransaction = new Transaction(null,
                coinbasePublicKey,
                LatteChainEnum.SUBSIDY,
                new ArrayList<>());
        // 添加输入输出信息
        // coinbase交易无输入，这里使用"-1"来代表
        genesisTransaction.getInputs().add(new TransactionInput("-1"));
        genesisTransaction.getOutputs().add(
                new TransactionOutput(
                        coinbasePublicKey,
                        genesisTransaction.getValue()));
        Block genesisBlock = createBlock("0",
                "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks");
        if (!mineService.addTransaction(genesisBlock, genesisTransaction)) {
            System.out.println("区块链系统初始化失败!");
            return false;
        }
        // TODO: 这里结算coinbase的UTXO稍有点特殊，计划测试了多个用户的交易后，写一个通用的刷新交易双方UTXO的函数
        walletService.getBalance(coinbaseWallet);
        
        if (!mineService.mineNewBlock(genesisBlock)) {
            System.out.println("区块链系统初始化失败!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isValidChain(ArrayList<Block> chainToCheck) {
        Block currentBlock;
        Block previousBlock;
        // a temporary working list of unspent transactions at a given block state.
        // HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();

        // 检查所有区块
        for (int i = 1; i < chainToCheck.size(); i++) {

            currentBlock = chainToCheck.get(i);
            previousBlock = chainToCheck.get(i - 1);
            if (!isValidBlock(previousBlock, currentBlock)) {
                return false;
            }

            //loop through blockchains transactions:
            // TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);
                if (!isValidTransaction(currentTransaction)) {
                    return false;
                }
            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    /**
     * @param preHash 前一区块哈希值
     * @param msg     区块中附带的信息
     * @return {@link Block}
     */
    @Override
    public Block createBlock(String preHash, String msg) {
        return new Block(preHash, msg);
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     * @return boolean 添加成功则返回true
     */
    @Override
    public boolean addBlock(Block blockToAdd) {
        int currentHeight = this.latteChain.getBlockchain().size();
        if (currentHeight == 0) {
            // 当前待添加块为创世块
            blockToAdd.setId(currentHeight);
            this.latteChain.getBlockchain().add(blockToAdd);
            return true;
        } else {
            Block preBlock = this.latteChain.getBlockchain().get(currentHeight - 1);
            if (isValidBlock(preBlock, blockToAdd)) {
                blockToAdd.setId(currentHeight);
                this.latteChain.getBlockchain().add(blockToAdd);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 检查是否是有效的区块
     *
     * @param preBlock 前一区块
     * @param block    当前区块
     * @return 有效则返回true
     */
    public boolean isValidBlock(Block preBlock, Block block) {
        Integer difficulty = LatteChain.getDifficulty();
        // 检查哈希值是否有效
        if (!block.getHash().substring(0, difficulty).equals(LatteChainEnum.TARGET_HASH)) {
            System.out.println("# 区块哈希值不符合条件");
            return false;
        }

        // 比较哈希值是否正确
        if (!block.getHash().equals(mineService.calculateBlockHash(block))) {
            System.out.println("# 当前区块哈希值不正确");
            return false;
        }

        // 检查哈希值的链接性，即比较当前区块与前一区块的哈希值是否相同
        if (!preBlock.getHash().equals(block.getPreviousHash())) {
            System.out.println("# 当前区块与上一区块父哈希不同");
            return false;
        }

        // 检查区块中所包含的交易是否都是合法的
        ArrayList<Transaction> transactionsList = block.getTransactions();
        for (Transaction transaction : transactionsList) {
            if (!isValidTransaction(transaction)) {
                System.out.println("# 当前区块中交易[ " + transaction.getId() + "] 不合法");
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否是有效的交易
     *
     * @param transaction {@link Transaction} 交易信息
     * @return 是则返回true
     */
    public boolean isValidTransaction(Transaction transaction) {
        // 检查当前交易的签名
        if (!transactionService.isValidSignature(transaction)) {
            System.out.println("# Signature on Transaction(" + transaction.getId() + ") is Invalid");
            return false;
        }

        // 检查输入输出的一致性
        if (transactionService.getInputsValue(transaction)
                != transactionService.getOutputsValue(transaction)) {
            System.out.println("# Inputs are note equal to outputs on Transaction(" + transaction.getId() + ")");
            return false;
        }
        return true;
    }
}
