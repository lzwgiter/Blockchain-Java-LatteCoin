package com.latte.blockchain.service;

import com.latte.blockchain.entity.*;

/**
 * 群签名服务接口类
 *
 * @author float311
 * @since 2021/04/09
 */
public interface IGsService {
    /**
     * 系统初始化
     */
    void setup();

    /**
     * 生成管理员机密信息，这包括管理员群私钥以及签名打开私钥，并生成系统公开参数发
     *
     * @param adminWallet 管理员钱包
     */
    void setAdminKeys(Wallet adminWallet);

    /**
     * 注册一个新用户
     *
     * @param newUser {@link Wallet} 用户对象
     * @param sk      {@link AdminGroupSecretKey} 管理员管理用户的私钥
     */
    void gEnroll(Wallet newUser, AdminGroupSecretKey sk);

    /**
     * 对消息msg进行群签名
     *
     * @param msg 消息
     * @param gsk {@link UserGroupSecretKey} 参与签名的用户群私钥
     */
    GroupSignature gSign(String msg, UserGroupSecretKey gsk);

    /**
     * 验证签名
     *
     * @param signature {@link GroupSignature} 待验证的签名
     * @param msg       原消息
     * @return 验证成功返回true
     */
    boolean gVerify(GroupSignature signature, String msg);

    /**
     * 打开签名查看交易发起方
     *
     * @param msg       原消息
     * @param signature {@link GroupSignature} 签名信息
     * @param ok        管理员打开签名的私钥
     * @return 用户姓名
     */
    String gOpen(String msg, GroupSignature signature, AdminGroupOpenKey ok);
}
