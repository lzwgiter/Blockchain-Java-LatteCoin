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
     * 初始化LatteChain区块链系统，初始化预置账户并创建创世块
     */
    @GetMapping("/init")
    public String initSystem() {
        if (chainService.initChain()) {
            return userService.getAllUsersInfo();
        } else {
            return "初始化失败";
        }
    }

    /**
     * 交易发起接口
     *
     * @param sender    发起方账户地址
     * @param recipient 接受方账户地址
     * @param value     交易金额
     * @return String
     */
    @PostMapping(path = "/trade")
    public String sendFunds(@RequestParam(name = "sender") String sender,
                            @RequestParam(name = "recipient") String recipient,
                            @RequestParam(name = "value") float value) {
        return transactionService.createTransaction(sender, recipient, value);
    }

    /**
     * 对指定的交易进行追踪
     *
     * @param id 待审计交易
     * @return 交易链信息
     */
    @PostMapping(path = "/auditTransaction")
    public String audit(@RequestParam(name = "transactionId") String id) {
        return transactionService.auditTransaction(id);
    }

    /**
     * 查询指定账户的余额
     *
     * @param address 查询对象的账户地址
     * @return String
     */
    @GetMapping(path = "/queryBalance")
    public String getUserBalance(@RequestParam(name = "address") String address) {
        return "余额：" + walletService.getBalance(address) + "LC";
    }
}
