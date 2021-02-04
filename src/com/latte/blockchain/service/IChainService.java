package com.latte.blockchain.service;

import com.latte.blockchain.entity.Block;

import java.util.ArrayList;

/**
 * @author float311
 * @since 2021/02/03
 */
public interface IChainService {
    /**
     * 初始化LatteChain区块链
     *
     * @return 初始化成功则返回true
     */
    boolean initChain();

    /**
     * 创建一个新的区块
     *
     * @param preHash 前一区块哈希值
     * @param msg     区块中附带的信息
     * @return {@link Block}
     */
    Block createBlock(String preHash, String msg);

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 待添加的区块
     * @return boolean 添加成功则返回true
     */
    boolean addBlock(Block blockToAdd);

    /**
     * 检查当前链上区块是否都有效
     *
     * @param chainToCheck 待检查链
     * @return 有效则返回true
     */
    boolean isValidChain(ArrayList<Block> chainToCheck);

}
