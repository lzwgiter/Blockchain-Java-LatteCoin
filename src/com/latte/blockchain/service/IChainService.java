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

    /**
     * 查看当前区块链
     *
     * @return 当前区块链信息
     */
    String queryChain();

    /**
     * 检查当前链上区块是否都有效
     *
     * @param chainToCheck 待检查链
     * @return 有效则返回true
     */
    boolean isValidChain(ArrayList<Block> chainToCheck);

}
