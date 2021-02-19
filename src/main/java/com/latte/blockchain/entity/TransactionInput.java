package com.latte.blockchain.entity;

import lombok.Data;

/**
 * @author float
 * @since 2021/1/27
 */
@Data
public class TransactionInput {

    /**
     * 下标
     */
    private String transactionOutputId;

    /**
     * 未支出的交易输出
     */
    private TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
