package com.latte.blockchain.impl;

import com.latte.blockchain.dao.BlockDao;
import com.latte.blockchain.dao.TransactionDao;
import com.latte.blockchain.dao.TransactionPoolDao;
import com.latte.blockchain.dao.UtxoDao;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.utils.LatteChain;
import com.latte.blockchain.utils.BeanContext;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.*;
import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
     * 交易池DAO对象
     */
    private TransactionPoolDao transactionPoolDao;

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
        transactionPoolDao = BeanContext.getApplicationContext().getBean(TransactionPoolDao.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoDao.class);

        ReentrantReadWriteLock lock = LockUtil.getLockUtil().getReadWriteLock();
        ReentrantLock requestLock = LockUtil.getLockUtil().getRequestLock();
        Condition condition = LockUtil.getLockUtil().getCondition();

        boolean flag = false;
        while (true) {
            try {
                lock.readLock().lock();
                // 并行读取当前链的高度信息、前一区块的哈希值信息、当前交易池中的交易信息
                long currentHeight = blockDao.getHeight();
                String preHash = blockDao.getBlockById(currentHeight - 1).getHash();
                if (transactionPoolDao.getPoolSize() == 0) {
                    System.out.println(Thread.currentThread().getName() + " Sleeping");
                    lock.readLock().unlock();
                    // 进入休眠状态
                    requestLock.lock();
                    try {
                        condition.await();
                    } finally {
                        requestLock.unlock();
                    }
                } else {
                    List<TransactionsPoolEntity> transactions = transactionPoolDao.getTransactions();
                    List<Transaction> works = new ArrayList<>(LatteChainEnum.MAX_TRANSACTION_AMOUNT);
                    for (TransactionsPoolEntity entity : transactions) {
                        works.add(transactionDao.getTransactionById(entity.getTransactionIndex()));
                    }
                    lock.readLock().unlock();
                    // 构造区块
                    Block newBlock = createNewBlock(preHash, Thread.currentThread().getName());
                    newBlock.setId(currentHeight);

                    // 将所有从交易池中获取的交易信息都添加到当前新构造的区块中
                    if (!addTransaction(newBlock, works)) {
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
                    addBlock(newBlock);
                    // 将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                    lock.writeLock().lock();
                    try {
                        for (Transaction transaction : works) {
                            // 将交易输出添加到全局
                            addToGlobal(transaction);
                            transactionPoolDao.deleteById(transaction.getId());
                        }
                    } finally {
                        lock.writeLock().unlock();
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
        Utxo output = new Utxo(coinbasePublicKey, LatteChainEnum.BLOCK_SUBSIDY);
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
        for (Utxo output : transaction.getOutputUtxos()) {
            utxoDao.save(output);
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
        Utxo reward = new Utxo(account, rewardValue);
        utxoDao.save(reward);
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     */
    @Override
    public void addBlock(Block blockToAdd) {
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
        } else {
            lock.writeLock().lock();
            try {
                blockDao.save(blockToAdd);
                System.out.println("[" + Thread.currentThread().getName() + " pushed √] : " + blockToAdd.getHash());
                // 奖励矿工
                rewardMiner(blockToAdd.getMsg(), blockToAdd);
            } finally {
                lock.writeLock().unlock();
            }
        }
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
                    if (!transactionPoolDao.existsById(transaction.getId())) {
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
        return CryptoUtil.applySm3Hash(
                block.getId() +
                        block.getPreviousHash() +
                        block.getTimeStamp() +
                        block.getMerkleRoot() +
                        block.getNonce());
    }
}
