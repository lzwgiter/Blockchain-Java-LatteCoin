package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;

import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/02/03
 */
public interface IChainService {

    /**
     * 初始化一个区块链系统
     *
     * @return 成功则返回true
     */
    boolean initChain();

}
