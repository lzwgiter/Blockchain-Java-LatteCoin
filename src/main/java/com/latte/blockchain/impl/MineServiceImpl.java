package com.latte.blockchain.impl;

import com.latte.blockchain.dao.BlockDao;
import com.latte.blockchain.dao.TransactionDao;
import com.latte.blockchain.dao.UtxoDao;
import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.BeanContext;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionOutput;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.*;
import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.latte.blockchain.utils.LockUtil;
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

    /**
     * 用户服务
     */
    private IUserService userService;

    /**
     * 交易服务
     */
    private ITransactionService transactionService;

    /**
     * 数据库区块DAO对象
     */
    private BlockDao blockDao;

    /**
     * 交易DAO对象
     */
    private TransactionDao transactionDao;

    /**
     * 全局UTXO DAO对象
     */
    private UtxoDao utxoDao;


    /**
     * 挖矿函数，将构造新的区块并尝试计算其哈希值
     */
    @Override
    public void run() {
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        blockDao = BeanContext.getApplicationContext().getBean(BlockDao.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionDao.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoDao.class);
        ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();

        boolean flag = false;
        while (true) {
            try {
                lock.readLock().lock();
                // 并行读取当前链的高度信息、前一区块的哈希值信息、当前交易池中的交易信息
                long currentHeight = blockDao.getHeight();
                String preHash = blockDao.getBlockById(currentHeight - 1).getHash();
                List<Transaction> transactions = transactionDao.getTransactions();
                lock.readLock().unlock();
                if (transactions.size() == 0) {
                    Thread.sleep(2000);
                } else {
                    // 构造区块
                    Block newBlock = createNewBlock(preHash, Thread.currentThread().getName());
                    newBlock.setId(currentHeight);

                    // 将所有从交易池中获取的交易信息都添加到当前新构造的区块中
                    if (!addTransaction(newBlock, transactions)) {
                        // 交易信息无效(签名信息错误、不满足最低金额、交易已被计算)
                        flag = true;
                    }
                    if (flag) {
                        // 挖矿失败，重新获取交易并创建、挖掘区块
                        continue;
                    }

                    // 计算新区块的哈希值
                    mineNewBlock(newBlock);
                    // 提交新的区块并获取奖励
                    if (this.addBlock(newBlock)) {
                        // 将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                        lock.writeLock().lock();
                        try {
                            for (Transaction transaction : transactions) {
                                // 将交易输出添加到全局
                                addToGlobal(transaction);
                                transactionDao.deleteById(transaction.getId());
                            }
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionDao.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoDao.class);

        // 初始化系统预置用户信息
        String coinbaseAddress = userService.initUser();
        PublicKey coinbasePublicKey = userService.getUserPublicKey(coinbaseAddress);
        // 初始块奖励
        TransactionOutput output = new TransactionOutput(coinbasePublicKey, LatteChainEnum.BLOCK_SUBSIDY);
        utxoDao.save(output);
        Block genesisBlock = this.createNewBlock("0",
                "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks");
        this.mineNewBlock(genesisBlock);
        // 将创世块添加到区块链上
        this.addBlock(genesisBlock);
        // 开启所有用户的挖矿线程
        for (Wallet user : latteChain.getUsers().values()) {
            user.getWorkerThread().start();
        }
        return true;
    }

    public void addToGlobal(Transaction transaction) {
        for (TransactionOutput output : transaction.getOutputs()) {
            utxoDao.save(output);
            System.out.println("交易输出" + output.getId() + "提交成功！");
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
        TransactionOutput reward = new TransactionOutput(account, rewardValue);
        utxoDao.save(reward);
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     * @return boolean 添加成功则返回true
     */
    @Override
    public boolean addBlock(Block blockToAdd) {
        blockDao = BeanContext.getApplicationContext().getBean(BlockDao.class);
        ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();
        if (blockToAdd.getPreviousHash().equals(LatteChainEnum.ZERO_HASH)) {
            // 当前待添加块为创世块
            blockToAdd.setId(0);
            // 添加到数据库中
            lock.writeLock().lock();
            try {
                blockDao.save(blockToAdd);
                System.out.println("[Genesis Block pushed √] : " + blockToAdd.getHash());
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        } else {
            if (isValidBlock(blockToAdd)) {
                lock.writeLock().lock();
                try {
                    blockDao.save(blockToAdd);
                    System.out.println("[" + Thread.currentThread().getName() + " pushed √] : " + blockToAdd.getHash());
                    // 奖励矿工
                    rewardMiner(blockToAdd.getMsg(), blockToAdd);
                } finally {
                    lock.writeLock().unlock();
                }
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
     * @param block 当前区块
     * @return 有效则返回true
     */
    @Override
    public boolean isValidBlock(Block block) {
        ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();
        // 检查哈希值是否有效
        if (!block.getHash().substring(0, LatteChainEnum.DIFFICULTY).equals(LatteChainEnum.TARGET_HASH)) {
            System.out.println("# 区块哈希值不符合条件");
            return false;
        }

        // 比较哈希值是否正确
        if (!block.getHash().equals(this.calculateBlockHash(block))) {
            // 当前区块哈希值不正确
            return false;
        }

        // 检查哈希值的链接性，即比较当前区块与前一区块的哈希值是否相同
        lock.readLock().lock();
        try {
            Block preBlock = blockDao.getBlockById(block.getId() - 1);
            if (!preBlock.getHash().equals(block.getPreviousHash())) {
                // 当前区块与上一区块父哈希不同
                return false;
            }
        } finally {
            lock.readLock().unlock();
        }

        // 检查区块中所包含的交易是否都是合法的
        ArrayList<Transaction> transactionsList = (ArrayList<Transaction>) block.getTransactions();
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
        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot((ArrayList<Transaction>) block.getTransactions()));
        String hash = this.calculateBlockHash(block);
        while (!hash.substring(0, LatteChainEnum.DIFFICULTY).equals(difficultyString)) {
            block.setNonce(block.getNonce() + 1);
            hash = this.calculateBlockHash(block);
        }
        block.setHash(hash);
        System.out.println("[" + Thread.currentThread().getName() + " Mined √] : " + hash);
    }

    /**
     * 执行交易并添加到区块中
     *
     * @param block        区块
     * @param transactions 交易信息
     */
    @Override
    public boolean addTransaction(Block block, List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (!block.getPreviousHash().equals(LatteChainEnum.ZERO_HASH)) {
                // 非初始块
                ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();
                lock.readLock().lock();
                try {
                    if (!transactionDao.existsById(transaction.getId())) {
                        // 当前交易已被消耗
                        return false;
                    }
                } finally {
                    lock.readLock().unlock();
                }

                if (!transactionService.processTransaction(transaction)) {
                    // 交易不合法或当前交易已经被消耗
                    return false;
                }
            }

            // 将交易添加至区块中
            block.getTransactions().add(transaction);
        }
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
