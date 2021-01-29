package com.latte.blockchain.entity;

import com.latte.blockchain.NoobChain;
import com.latte.blockchain.TransactionInput;
import com.latte.blockchain.TransactionOutput;
import com.latte.blockchain.utils.CryptoUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * 交易类
 *
 * @author float
 * @since 2021/1/27
 */
public class Transaction {

    /**
     * 交易的id
     */
    private String transactionId;

    /**
     * 发送方的地址(公钥)
     */
    private PublicKey sender;

    /**
     * 接受方的地址(公钥)
     */
    private PublicKey reciepient;

    /**
     * 发送的金额
     */
    private Float value;

    /**
     * 交易签名信息
     */
    private byte[] signature;

    /**
     * 交易输入
     */
    private ArrayList<TransactionInput> inputs;

    /**
     * 交易输出
     */
    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    /**
     * 记录交易数量
     */
    private Integer sequence = 0;

    /**
     * 数据
     */
    private String data;

    /**
     * 交易
     *
     * @param sender    {@link PublicKey} 发送方地址
     * @param recipient {@link PublicKey} 接受方地址
     * @param value     交易金额
     * @param inputs    交易输入
     */
    public Transaction(PublicKey sender, PublicKey recipient, Float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.reciepient = recipient;
        this.value = value;
        this.inputs = inputs;
        this.data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(recipient) + value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
    }

    public PublicKey getReciepient() {
        return reciepient;
    }

    public void setReciepient(PublicKey reciepient) {
        this.reciepient = reciepient;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
