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

    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    /**
     * 记录交易数量
     */
    private static int sequence = 0;

    public Transaction(PublicKey sender, PublicKey recipient, Float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.reciepient = recipient;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean verifyTransaction() {
        if (!isValidSignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //Gathers transaction inputs (Making sure they are unspent):
        for (TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }

        //Checks if transaction is valid:
        if (getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        //Generate transaction outputs:
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calHash();
        outputs.add(new TransactionOutput(this.reciepient, value, transactionId)); //send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); //send the left over 'change' back to sender

        //Add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            NoobChain.UTXOs.put(o.id, o);
        }

        //Remove transaction inputs from UTXO lists as spent:
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) {
                continue; //if Transaction can't be found skip it
            }
            NoobChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) {
                continue; //if Transaction can't be found skip it, This behavior may not be optimal.
            }
            total += i.UTXO.value;
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(reciepient) + value;
        signature = CryptoUtil.applySignature(privateKey, data);
    }

    public boolean isValidSignature() {
        String data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(reciepient) + value;
        return CryptoUtil.verifySignature(sender, data, signature);
    }

    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    private String calHash() {
        //increase the sequence to avoid 2 identical transactions having the same hash
        sequence++;
        return CryptoUtil.applySha256(
                CryptoUtil.getStringFromKey(sender) +
                        CryptoUtil.getStringFromKey(reciepient) +
                        value +
                        sequence
        );
    }
}
