package com.latte.blockchain.dao;

import com.latte.blockchain.entity.TransactionOutput;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author float311
 * @since 2021/02/21
 */
public interface UtxoDao extends JpaRepository<TransactionOutput, String> {
    /**
     * 从全局UTXO中获取一个utxo
     * @param id utxo的id
     * @return {@link TransactionOutput}
     */
    TransactionOutput getTransactionOutputById(String id);
}
