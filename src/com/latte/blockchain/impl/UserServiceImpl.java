package com.latte.blockchain.impl;

import com.latte.blockchain.entity.LatteChain;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.enums.LatteChainEnum;
import com.latte.blockchain.service.IUserService;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

/**
 * @author float311
 * @since 2021/02/03
 */
@Service
public class UserServiceImpl implements IUserService {

    private final LatteChain latteChain = LatteChain.getInstance();

    /**
     * 初始化区块链系统中预置账户信息
     * @return String coinbase账户的账户地址
     */
    @Override
    public String initUser() {
        // 添加并初始化coinbase账户
        Wallet newUser = new Wallet();
        String coinbaseAddress = CryptoUtil.getStringFromKey(newUser.getPublicKey());
        latteChain.getUsers().put(coinbaseAddress, newUser);
        for (int i = 1; i < LatteChainEnum.INIT_ACCOUNT_AMOUNTS; i++) {
            newUser = new Wallet();
            latteChain.getUsers().put(
                    CryptoUtil.getStringFromKey(newUser.getPublicKey()), newUser);
        }
        return coinbaseAddress;
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
        return JsonUtil.toJson(latteChain.getUsers());
    }
}
