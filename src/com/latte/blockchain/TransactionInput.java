package com.latte.blockchain;

/**
 * @author float
 * @since 2021/1/27
 */
public class TransactionInput {

    /**
     * 下标
     */
    public String transactionOutputId;

    /**
     * 未支出的交易输出
     */
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
