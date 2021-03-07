package com.latte.blockchain.dao;

import com.latte.blockchain.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 获取当前区块链的高度
     * @return 区块链当前的高度
     */
    @Transactional(timeout = 5, propagation = Propagation.NOT_SUPPORTED)
    @Query(value = "select count(*) from blocks", nativeQuery = true)
    long getHeight();
}
