package com.latte.blockchain.service.impl;

import com.latte.blockchain.repository.BlockRepo;
import com.latte.blockchain.repository.TransactionRepo;
import com.latte.blockchain.repository.TransactionPoolRepo;
import com.latte.blockchain.repository.UtxoRepo;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.*;
import com.latte.blockchain.utils.LatteChain;
import com.latte.blockchain.utils.BeanContext;
import com.latte.blockchain.enums.LatteChainConfEnum;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.LockUtil;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 挖矿服务类
 *
 * @author float311
 * @since 2021/01/28
 */
@Service
@Slf4j
public class MineServiceImpl implements IMineService {

    private final LatteChain latteChain = LatteChain.getInstance();

    /**
     *
     * 用户服务
     */
    private IUserService userService;

    /**
     * 交易服务
     */
    private ITransactionService transactionService;

    /**
     * 群签名五福
     */
    private IGsService iGsService;

    /**
     * 数据库区块DAO对象
     */
    private BlockRepo blockDao;

    /**
     * 交易DAO对象
     */
    private TransactionRepo transactionDao;

    /**
     * 交易池DAO对象
     */
    private TransactionPoolRepo transactionPoolDao;

    /**
     * 全局UTXO DAO对象
     */
    private UtxoRepo utxoDao;


    /**
     * 挖矿函数，将构造新的区块并尝试计算其哈希值
     */
    @Override
    public void run() {
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        iGsService = BeanContext.getApplicationContext().getBean(IGsServiceImpl.class);
        blockDao = BeanContext.getApplicationContext().getBean(BlockRepo.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionRepo.class);
        transactionPoolDao = BeanContext.getApplicationContext().getBean(TransactionPoolRepo.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoRepo.class);

        ReentrantLock stateLock = LockUtil.getLockUtil().getStateLock();
        Condition condition = LockUtil.getLockUtil().getWriteCondition();

        while (true) {
            stateLock.lock();
            try {
                if (transactionPoolDao.getPoolSize() == 0) {
                    condition.await();
                } else {
                    // 并行读取当前链的高度信息、前一区块的哈希值信息、当前交易池中的交易信息
                    long currentHeight = blockDao.getHeight();
                    String preHash = blockDao.getBlockById(currentHeight - 1).getHash();
                    List<TransactionsPoolEntity> transactions = transactionPoolDao.getTransactions();
                    List<Transaction> works = new ArrayList<>(LatteChainConfEnum.MAX_TRANSACTION_AMOUNT);
                    for (TransactionsPoolEntity entity : transactions) {
                        works.add(transactionDao.getTransactionById(entity.getTransactionIndex()));
                    }
                    // 构造区块
                    Block newBlock = new Block(preHash, Thread.currentThread().getName());
                    newBlock.setId(currentHeight);

                    // 将所有从交易池中获取的交易信息都添加到当前新构造的区块中
                    if (!addTransaction(newBlock, works)) {
                        // 交易信息无效(签名信息错误、不满足最低金额、交易已被计算)
                        // 挖矿失败，重新获取交易并创建、挖掘区块
                        continue;
                    }

                    // 计算新区块的哈希值
                    mineNewBlock(newBlock);
                    // 提交新的区块并获取奖励
                    addBlock(newBlock);
                    // 将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                    for (Transaction transaction : works) {
                        // 将交易输出添加到全局
                        addToGlobalUtxo(transaction);
                        transactionPoolDao.deleteById(transaction.getId());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                stateLock.unlock();
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
        if (latteChain.isInit()) {
            // 已经初始化
            return false;
        }

        // 引入服务类实例
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        iGsService = BeanContext.getApplicationContext().getBean(IGsServiceImpl.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionRepo.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoRepo.class);

        // 初始化群签名服务
        iGsService.setup();

        // 初始化系统预置用户信息
        userService.initUser();
        Wallet adminAccount = userService.getAllUsersInfo().get("admin");
        // 初始化管理员信息并生成系统参数
        iGsService.setAdminKeys(adminAccount);
        // 将当前环境所有用户都加入到群中
        for (Wallet newUser : userService.getAllUsersInfo().values()) {
            iGsService.gEnroll(newUser, adminAccount.getAsk());
        }

        PublicKey coinbasePublicKey = userService.getUserPublicKey("admin");
        // 初始块奖励
        Utxo output = new Utxo(coinbasePublicKey, LatteChainConfEnum.BLOCK_SUBSIDY);
        utxoDao.save(output);
        Block genesisBlock = new Block("0",
                "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks");
        this.mineNewBlock(genesisBlock);
        // 将创世块添加到区块链上
        this.addBlock(genesisBlock);
        // 开启所有用户的挖矿线程
        for (Wallet user : latteChain.getUsers().values()) {
            user.getWorkerThread().start();
        }
        latteChain.setInit(true);
        return true;
    }

    /**
     * 将交易输出添加到全局UTXO
     *
     * @param transaction 交易
     */
    public void addToGlobalUtxo(Transaction transaction) {
        for (Utxo output : transaction.getOutputUtxos()) {
            utxoDao.save(output);
        }
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     */
    @Override
    public void addBlock(Block blockToAdd) {
        blockDao = BeanContext.getApplicationContext().getBean(BlockRepo.class);
        if (blockToAdd.getPreviousHash().equals(LatteChainConfEnum.ZERO_HASH)) {
            // 当前待添加块为创世块
            blockToAdd.setId(0);
            // 添加到数据库中
            blockDao.save(blockToAdd);
            log.info("[Initiation] 创世块已创建！LatteChain实例初始化成功");
        } else {
            blockDao.save(blockToAdd);
            log.info("[Issued Block] " + Thread.currentThread().getName() + " Mined ☺ : " + blockToAdd.getHash());
            // 奖励矿工
            rewardMiner(blockToAdd.getMsg(), blockToAdd);
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
        float rewardValue = LatteChainConfEnum.BLOCK_SUBSIDY +
                LatteChainConfEnum.TRANSACTION_SUBSIDY * block.getTransactions().size();
        PublicKey account = userService.getUserPublicKey(address);
        Utxo reward = new Utxo(account, rewardValue);
        utxoDao.save(reward);
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
            if (!block.getPreviousHash().equals(LatteChainConfEnum.ZERO_HASH)) {
                // 非初始块
                if (!transactionPoolDao.existsById(transaction.getId())) {
                    // 当前交易已被消耗
                    return false;
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
     * 计算新的区块哈希值并计算默克根
     *
     * @param block 前一区块的Hash值
     */
    @Override
    public void mineNewBlock(Block block) {
        String difficultyString = LatteChainConfEnum.TARGET_HASH;
        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot((ArrayList<Transaction>) block.getTransactions()));
        String hash = this.calculateBlockHash(block);
        while (!hash.substring(0, LatteChainConfEnum.DIFFICULTY).equals(difficultyString)) {
            block.setNonce(block.getNonce() + 1);
            hash = this.calculateBlockHash(block);
        }
        block.setHash(hash);
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
