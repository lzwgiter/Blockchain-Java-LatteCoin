package com.latte.blockchain.repository;

import com.latte.blockchain.entity.Utxo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author float311
 * @since 2021/02/21
 */
public interface UtxoRepo extends JpaRepository<Utxo, String> {
    /**
     * 从全局UTXO中获取一个utxo
     * @param id utxo的id
     * @return {@link Utxo}
     */
    Utxo getTransactionOutputById(String id);
}
