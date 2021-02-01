package com.latte.blockchain;

import com.latte.blockchain.entity.Block;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.TransactionInput;
import com.latte.blockchain.entity.TransactionOutput;

import com.latte.blockchain.impl.TransactionServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;
import java.util.HashMap;

/**
 * @author float311
 * @since 2021/01/31
 */
@SpringBootApplication
public class LatteCoinDemo {
    public static void main(String[] args) {
        SpringApplication.run(LatteCoinDemo.class, args);
    }
//    /**
//     * TODO 将MineServiceImpl和TransactionServiceImpl这两个服务编织进来
//     * @param args
//     */
//    public static void main(String[] args) {
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//        //创建钱包
//        Wallet walletA = new Wallet();
//        Wallet walletB = new Wallet();
//        Wallet coinbase = new Wallet();
//
//        // 创建一次初始交易 A向B支付100币
//        Transaction genesisTransaction = new Transaction(
//                coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
//        // 对交易进行手动签名
//        TransactionServiceImpl transactionService = new TransactionServiceImpl();
//        transactionService.generateSignature(coinbase.getPrivateKey(), genesisTransaction);
//        // 设置交易id
//        genesisTransaction.setId("0");
//        // 添加交易输出
//        genesisTransaction.getOutputs().add(
//                new TransactionOutput(
//                        genesisTransaction.getRecipient(),
//                        genesisTransaction.getValue(),
//                        genesisTransaction.getId()));
//        // 将交易储存进UTXO列表.
//        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));
//
//        System.out.println("Creating and Mining Genesis block... ");
//        // TODO 使用MineServiceImpl挖掘区块
//        Block genesis = new Block("0");
//        genesis.addTransaction(genesisTransaction);
//        addBlock(genesis);
//
//        // 测试
//        Block block1 = new Block(genesis.hash);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
//        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
//        addBlock(block1);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("WalletB's balance is: " + walletB.getBalance());
//
//        Block block2 = new Block(block1.hash);
//        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
//        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
//        addBlock(block2);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("WalletB's balance is: " + walletB.getBalance());
//
//        Block block3 = new Block(block2.hash);
//        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
//        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("WalletB's balance is: " + walletB.getBalance());
//
//        isChainValid();
//
//    }
//    public static Boolean isChainValid() {
//        Block currentBlock;
//        Block previousBlock;
//        String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');
//        //a temporary working list of unspent transactions at a given block state.
//        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
//        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
//
//        //loop through blockchain to check hashes:
//        for (int i = 1; i < blockchain.size(); i++) {
//
//            currentBlock = blockchain.get(i);
//            previousBlock = blockchain.get(i - 1);
//            //compare registered hash and calculated hash:
//            if (!currentBlock.hash.equals(currentBlock.calHash())) {
//                System.out.println("#Current Hashes not equal");
//                return false;
//            }
//            //compare previous hash and registered previous hash
//            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
//                System.out.println("#Previous Hashes not equal");
//                return false;
//            }
//            //check if hash is solved
//            if (!currentBlock.hash.substring(0, DIFFICULTY).equals(hashTarget)) {
//                System.out.println("#This block hasn't been mined");
//                return false;
//            }
//
//            //loop thru blockchains transactions:
//            TransactionOutput tempOutput;
//            for (int t = 0; t < currentBlock.transactions.size(); t++) {
//                Transaction currentTransaction = currentBlock.transactions.get(t);
//
//                if (!currentTransaction.isValidSignature()) {
//                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
//                    return false;
//                }
//                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
//                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
//                    return false;
//                }
//
//                for (TransactionInput input : currentTransaction.inputs) {
//                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());
//
//                    if (tempOutput == null) {
//                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
//                        return false;
//                    }
//
//                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
//                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
//                        return false;
//                    }
//
//                    tempUTXOs.remove(input.getTransactionOutputId());
//                }
//
//                for (TransactionOutput output : currentTransaction.getOutputs()) {
//                    tempUTXOs.put(output.getId(), output);
//                }
//
//                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
//                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
//                    return false;
//                }
//                if (currentTransaction.getOutputs().get(1).recipient != currentTransaction.getSender()) {
//                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
//                    return false;
//                }
//
//            }
//
//        }
//        System.out.println("Blockchain is valid");
//        return true;
//    }
//
//    public static void addBlock(Block newBlock) {
//        newBlock.mineBlock(DIFFICULTY);
//        blockchain.add(newBlock);
//    }
}
