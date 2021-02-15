package com.latte.blockchain.impl;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.IChainService;
import com.latte.blockchain.service.ITransactionService;

import java.security.Security;
import java.util.ArrayList;

import com.latte.blockchain.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * @author float311
 * @since 2021/02/03
 */
@Service
public class ChainServiceImpl implements IChainService {

    @Autowired
    private IMineService mineService;

    @Autowired
    private ITransactionService transactionService;

    /**
     * 初始化一个区块链系统
     *
     * @return 成功则返回true
     */
    @Override
    public boolean initChain() {
        Security.addProvider(new BouncyCastleProvider());
        return mineService.initChain();
    }

    @Override
    public String queryChain() {
        return JsonUtil.toJson(LatteChain.getInstance());
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
            if (!mineService.isValidBlock(previousBlock, currentBlock)) {
                return false;
            }

            //loop through blockchains transactions:
            // TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);
                if (!transactionService.isValidTransaction(currentTransaction)) {
                    return false;
                }
            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }
}
