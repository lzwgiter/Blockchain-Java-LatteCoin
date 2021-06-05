package com.latte.blockchain.service.impl;

import com.latte.blockchain.service.IMineService;
import com.latte.blockchain.service.IChainService;

import java.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * 链服务
 *
 * @author float311
 * @since 2021/02/03
 */
@Service
public class ChainServiceImpl implements IChainService {

    @Autowired
    private IMineService mineService;

    /**
     * 初始化一个区块链系统
     *
     * @return 成功则返回true
     */
    @Override
    public boolean initChain() {
        Security.addProvider(new BouncyCastleProvider());
        return mineService.initChain();
    }
}
