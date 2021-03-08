package com.latte.blockchain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author float311
 * @since 2021/03/08
 */
@Entity
@Table(name = "transaction_pool")
public class TransactionsPoolEntity implements Serializable {
    /**
     * 交易id值
     */
    @Id
    @Setter
    @Getter
    private String transactionIndex;

    /**
     * 时间戳
     */
    @Column
    private long timeStamp;

    protected TransactionsPoolEntity() {}

    public TransactionsPoolEntity(String index, long timeStamp) {
        this.transactionIndex = index;
        this.timeStamp = timeStamp;
    }
}
