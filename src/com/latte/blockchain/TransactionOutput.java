package com.latte.blockchain;

import com.latte.blockchain.utils.CryptoUtil;

import java.security.PublicKey;

/**
 *
 * @author float
 * @since 2021/1/27
 */
public class TransactionOutput {

    public String id;
    public PublicKey reciepient; //also known as the new owner of these coins.
    public float value; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptoUtil.applySha256(CryptoUtil.getStringFromKey(reciepient)+ value +parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }
}
