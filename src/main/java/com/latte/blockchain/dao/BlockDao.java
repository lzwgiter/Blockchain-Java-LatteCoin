package com.latte.blockchain.dao;

import com.latte.blockchain.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Table;

/**
 * @author float311
 * @since 2021/02/21
 */
public interface BlockDao extends JpaRepository<Block, String> {
    /**
     * 获取链上最后一条记录
     *
     * @param id 区块的id
     * @return Block
     */
    Block getBlockById(long id);
}
