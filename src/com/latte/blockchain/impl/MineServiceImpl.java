package com.latte.blockchain.impl;

import com.latte.blockchain.entity.*;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.*;
import com.latte.blockchain.utils.CryptoUtil;

import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;


/**
 * 挖矿服务类
 *
 * @author float311
 * @since 2021/01/28
 */
@Service
public class MineServiceImpl implements IMineService {

    private final LatteChain latteChain = LatteChain.getInstance();

    private IUserService userService;

    private ITransactionService transactionService;

    private ITransactionPoolService poolService;

    /**
     * 挖矿函数，将构造新的区块并尝试计算其哈希值
     */
    @Override
    public void run() {
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        poolService = BeanContext.getApplicationContext().getBean(TransactionPoolServiceImpl.class);
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        boolean flag = false;
        while (true) {
            // 构造区块
            int currentHeight = latteChain.getBlockchain().size();
            String preHash = latteChain.getBlockchain().get(currentHeight - 1).getHash();
            Block newBlock = createNewBlock(preHash, Thread.currentThread().getName());
            newBlock.setId(currentHeight);

            // 抓取交易池中的交易
            int poolSize = poolService.getPoolSize();
            ArrayList<Transaction> transactions;
            if (poolSize >= LatteChainEnum.MAX_TRANSACTION_AMOUNT) {
                transactions = poolService.getTransactions(1);
            } else if (poolSize > 0) {
                transactions = poolService.getTransactions(0);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // 将所有从交易池中获取的交易信息都添加到当前新构造的区块中
            for (Transaction transaction : transactions) {
                if(!addTransaction(newBlock, transaction)) {
                    // 交易处理失败(其他线程已经挖出矿)
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }

            // 计算新区块的哈希值
            mineNewBlock(newBlock);
            if (latteChain.getBlockchain().size() == currentHeight) {
                // 提交新的区块并获取奖励
                if (this.addBlock(newBlock)) {
                    // 将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                    for (Transaction transaction : transactions) {
                        addToGlobal(transaction);
                        poolService.removeTransaction(transaction);
                    }
                }
            }
        }
    }

    /**
     * 初始化区块链系统
     *
     * @return boolean
     */
    @Override
    public boolean initChain() {
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);

        // 初始化系统预置用户信息
        String coinbaseAddress = userService.initUser();
        PublicKey coinbasePublicKey = userService.getUserPublicKey(coinbaseAddress);
        // coinbase交易
        Transaction genesisTransaction = new Transaction(null,
                coinbasePublicKey,
                LatteChainEnum.BLOCK_SUBSIDY,
                new ArrayList<>());
        // 添加输入输出信息
        // coinbase交易无输入，这里使用"-1"来代表
        genesisTransaction.getInputs().add(new TransactionInput("-1"));
        genesisTransaction.getOutputs().add(
                new TransactionOutput(
                        coinbasePublicKey,
                        genesisTransaction.getValue()));
        Block genesisBlock = this.createNewBlock("0",
                "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks");
        if (!this.addTransaction(genesisBlock, genesisTransaction)) {
            System.out.println("区块链系统初始化失败!");
            return false;
        }
        addToGlobal(genesisTransaction);
        this.mineNewBlock(genesisBlock);
        // 将创世块添加到区块链上
        this.addBlock(genesisBlock);
        // 开启所有用户的挖矿线程
        for (Map.Entry<String, Wallet> entry : latteChain.getUsers().entrySet()) {
            entry.getValue().getWorkerThread().start();
        }
        return true;
    }

    public void addToGlobal(Transaction transaction) {
        // 将当前交易记在系统全局UTXO中
        for (TransactionOutput output : transaction.getOutputs()) {
            latteChain.getUTXOs().put(output.getId(), output);
            System.out.println("交易" + output.getId() + "执行成功！");
        }
    }

    /**
     * 发放奖励给旷工
     *
     * @param address 矿工账户
     * @param block   区块
     */
    @Override
    public void rewardMiner(String address, Block block) {
        float rewardValue = LatteChainEnum.BLOCK_SUBSIDY +
                LatteChainEnum.TRANSACTION_SUBSIDY * block.getTransactions().size();
        PublicKey account = userService.getUserPublicKey(address);
        Transaction rewardTransaction = new Transaction(null, account, rewardValue, new ArrayList<>());
        rewardTransaction.getInputs().add(new TransactionInput("-1"));
        rewardTransaction.getOutputs().add(new TransactionOutput(account, rewardTransaction.getValue()));
        // 将奖励交易放入交易池中
        poolService.addTransaction(rewardTransaction);
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     * @return boolean 添加成功则返回true
     */
    @Override
    public synchronized boolean addBlock(Block blockToAdd) {
        int currentHeight = latteChain.getBlockchain().size();
        if (currentHeight == 0) {
            // 当前待添加块为创世块
            blockToAdd.setId(currentHeight);
            this.latteChain.getBlockchain().add(blockToAdd);
            return true;
        } else if (currentHeight != blockToAdd.getId()) {
            // 父块发生变化，添加失败
            return false;
        } else {
            Block preBlock = this.latteChain.getBlockchain().get(currentHeight - 1);
            if (isValidBlock(preBlock, blockToAdd)) {
                this.latteChain.getBlockchain().add(blockToAdd);
                rewardMiner(blockToAdd.getMsg(), blockToAdd);
                return true;
            } else {
                // 区块信息不正确，添加失败
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
    @Override
    public boolean isValidBlock(Block preBlock, Block block) {
        IMineService mineService = new MineServiceImpl();
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
            if (!transactionService.isValidTransaction(transaction)) {
                System.out.println("# 当前区块中交易[ " + transaction.getId() + "] 不合法");
                return false;
            }
        }

        return true;
    }

    /**
     * 创建一个新的空区块
     *
     * @param preHash 前一区块的哈希值
     * @param msg     区块附带的信息
     * @return {@link Block}
     */
    @Override
    public Block createNewBlock(String preHash, String msg) {
        return new Block(preHash, msg);
    }

    /**
     * 计算新的区块哈希值并计算默克根
     *
     * @param block 前一区块的Hash值
     */
    @Override
    public void mineNewBlock(Block block) {
        String difficultyString = CryptoUtil.getDifficultyString();
        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot(block.getTransactions()));
        String hash = this.calculateBlockHash(block);
        while (!hash.substring(0, LatteChain.getDifficulty()).equals(difficultyString)) {
            block.setNonce(block.getNonce() + 1);
            hash = this.calculateBlockHash(block);
        }
        block.setHash(hash);
        System.out.println("[" + Thread.currentThread().getName() + " Mined √] : " + hash);
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
            // 非初始块则检查交易合法性(输入输出是否匹配、签名是否正确)并计算交易的信息(交易id、消耗，找零的UTXO)
            if (!transactionService.processTransaction(transaction)) {
                System.out.println(Thread.currentThread().getName() + ": 交易处理失败");
                return false;
            }
        }

        // 将交易添加至区块中
        block.getTransactions().add(transaction);
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
