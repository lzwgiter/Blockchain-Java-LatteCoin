package com.latte.blockchain.controller;

import com.latte.blockchain.service.IChainService;
import com.latte.blockchain.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    private IChainService chainService;

    /**
     * 初始化LatteChain区块链系统
     *
     */
    @GetMapping("/init")
    public String initSystem() {
        if (chainService.initChain()) {
            return "初始化成功！";
        } else {
            return "初始化失败";
        }
    }

    @GetMapping(path = "/allUsersInfo")
    public String getUserBalance() {
        return userService.getAllUsersInfo();
    }
}
