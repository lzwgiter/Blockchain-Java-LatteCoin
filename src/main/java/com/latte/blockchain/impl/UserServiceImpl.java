package com.latte.blockchain.impl;

import com.latte.blockchain.utils.LatteChain;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.enums.LatteChainConfEnum;
import com.latte.blockchain.service.IUserService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

/**
 * @author float311
 * @since 2021/02/03
 */
@Service
public class UserServiceImpl implements IUserService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IWalletService walletService;

    /**
     * 初始化区块链系统中预置账户信息
     *
     */
    @Override
    public void initUser() {
        // 添加并初始化所有账户
        Wallet newUser = new Wallet();
        newUser.setName("admin");
        // 设置系统管理员公钥地址
        latteChain.setAdminPublicKey(newUser.getPublicKey());
        latteChain.getUsers().put("admin", newUser);
        for (int i = 1; i < LatteChainConfEnum.INIT_ACCOUNT_AMOUNTS; i++) {
            newUser = new Wallet();
            latteChain.getUsers().put(newUser.getName(), newUser);
        }
    }

    /**
     * 返回查询用户PublicKey信息
     *
     * @param address String 账户地址信息
     * @return {@link PublicKey}
     */
    @Override
    public PublicKey getUserPublicKey(String address) {
        return latteChain.getUsers().get(address).getPublicKey();
    }

    @Override
    public String getAllUsersInfo() {
        // 刷新所有用戶的信息
        for (String address : latteChain.getUsers().keySet()) {
            walletService.getBalance(address);
        }
        return JsonUtil.toJson(latteChain.getUsers());
    }
}
