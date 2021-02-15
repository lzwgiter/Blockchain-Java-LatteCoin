package com.latte.blockchain.controller;

import com.latte.blockchain.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author float311
 * @since 2021/01/31
 */
@RestController
public class LatteChainController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private IChainService chainService;

    @Autowired
    private ITransactionService transactionService;

    /**
     * 初始化LatteChain区块链系统
     */
    @GetMapping("/init")
    public String initSystem() {
        if (chainService.initChain()) {
            return userService.getAllUsersInfo();
        } else {
            return "初始化失败";
        }
    }

    @PostMapping(path = "/trade")
    public String sendFunds(@RequestParam(name = "sender") String sender,
                            @RequestParam(name = "recipient") String recipient,
                            @RequestParam(name = "value") float value) {
        return transactionService.createTransaction(sender, recipient, value);
    }

    @GetMapping(path = "/queryBalance")
    public String getUserBalance(@RequestParam(name = "address") String address) {
        return "余额：" + walletService.getBalance(address) + "LC";
    }

    @GetMapping(path = "/checkChain")
    public String getLatteChain() {
        return chainService.queryChain();
    }
}
