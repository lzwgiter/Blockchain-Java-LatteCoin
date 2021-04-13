package com.latte.blockchain.impl;

import com.latte.blockchain.entity.*;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.service.IGsService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * 群签名服务
 *
 * @author float311
 * @since 2021/04/11
 */
@Service
@Slf4j
public class IGsServiceImpl implements IGsService {

    /**
     * 配对
     */
    @Getter
    private Pairing pair;

    /**
     * 系统公开参数
     */
    @Getter
    private GroupPublicKey gpk;

    /**
     * G1群
     */
    @Getter
    private Field g1;

    /**
     * G2群
     */
    @Getter
    private Field g2;


    /**
     * 整数群
     */
    private Field zr;

    /**
     * G1群生成元
     */
    @Getter
    private Element p1;

    /**
     * G2群生成元
     */
    @Getter
    private Element p2;

    private boolean isInit = false;

    private final HashMap<Element, String> joinedUserHashMap = new HashMap<>();

    /**
     * 签名服务初始化
     */
    @Override
    public void setup() {
        if (!isInit) {
            // 初始化配对
            this.pair = PairingFactory.getPairing("crypto/a.properties");

            // 生成两个加法循环群以及一个整数群
            this.g1 = pair.getG1();
            this.g2 = pair.getG2();
            this.zr = pair.getZr();

            // 生成两个加法循环群的生成元
            this.p1 = g1.newRandomElement();
            this.p2 = g2.newRandomElement();
            this.isInit = true;
        }
    }

    /**
     * 生成管理员机密信息，这包括管理员群私钥以及签名打开私钥
     *
     * @param adminWallet {@link Wallet} 管理员钱包
     */
    @Override
    public void setAdminKeys(Wallet adminWallet) {
        Element d = zr.newRandomElement();
        Element s = zr.newRandomElement();
        Element u = zr.newRandomElement();

        Element adminD = g1.newElementFromBytes(d.toBytes());
        Element adminS = g1.newElementFromBytes(s.toBytes());
        Element adminU = g1.newElementFromBytes(u.toBytes());
        adminWallet.setAsk(new AdminGroupSecretKey(adminD, adminS));
        adminWallet.setOk(new AdminGroupOpenKey(adminU));

        generatePublicParameters(adminWallet);
    }

    /**
     * 生成群签名服务的公开系统参数
     *
     * @param adminWallet 管理员钱包
     */
    private void generatePublicParameters(Wallet adminWallet) {
        // 获取管理员私有信息
        Element d = adminWallet.getAsk().getD();
        Element s = adminWallet.getAsk().getS();
        Element u = adminWallet.getOk().getU();

        // 计算生成系统参数
        Element sysD = d.duplicate().mul(this.p1);
        Element sysS = s.duplicate().mul(this.p2);
        Element sysU = u.duplicate().mul(this.p1);

        this.gpk = new GroupPublicKey(sysD, sysS, sysU);
    }

    /**
     * 为新用户生成群私钥并加入群
     *
     * @param newUser {@link Wallet} 用户对象
     * @param sk      {@link AdminGroupSecretKey} 管理员管理用户的私钥
     */
    @Override
    public void gEnroll(Wallet newUser, AdminGroupSecretKey sk) {
        // 随机选取秘密x
        Element x = g1.newElementFromBytes(zr.newRandomElement().toBytes());

        // 获取管理员参数
        Element d = sk.getD();
        Element s = sk.getS();

        // 计算z = (d - x) * (s * x)^-1 * p1
        Element dSubX = d.duplicate().sub(x);
        Element invertsMulX = s.duplicate().mul(x).invert();
        Element z = dSubX.mul(invertsMulX).mul(this.p1);

        // 赋予群用户
        newUser.setGsk(new UserGroupSecretKey(x, z));
        // 添加到群中
        this.joinedUserHashMap.put(x.mul(z), newUser.getName());
        log.info("用户" + newUser.getName() + "已加入群！");
    }

    /**
     * @param msg      消息
     * @param gsk      {@link UserGroupSecretKey} 参与签名的用户
     * @return {@link GroupSignature} 签名信息
     */
    @Override
    public GroupSignature gSign(String msg, UserGroupSecretKey gsk) {
        // 随机从整数群中选择一个k
        Element k = g1.newElementFromBytes(this.zr.newRandomElement().toBytes());
        Element x = gsk.getX();
        Element z = gsk.getZ();

        // 系统参数
        Element u = this.gpk.getU();
        Element s = this.gpk.getS();

        // 计算C1, C2以及映射
        // c1 = k * p1;
        Element c1 = k.duplicate().mul(this.p1);

        // c2 = x * Z + k * U
        Element xMulZ = x.duplicate().mul(z);
        Element kMulU = k.duplicate().mul(u);
        Element c2 = xMulZ.add(kMulU);

        // 计算映射: e(U, S)^k
        Element q = pair.pairing(u, s).powZn(k);
        Element qG1 = g1.newElementFromBytes(q.toBytes());
        
        // 计算Hash: c = hash(M, C1, C2, Q)
        String mDigest = CryptoUtil.applySm3Hash(msg);
        Element m = g1.newElementFromBytes(
                zr.newElementFromBytes(mDigest.getBytes(StandardCharsets.UTF_8))
                        .toBytes());
        Element c = m.duplicate().add(c1).add(c2).add(qG1);

        // 计算w
        Element w = k.duplicate().mul(c).add(x);

        return new GroupSignature(c1, c2, c, w);
    }

    /**
     * @param signature {@link GroupSignature}待验证的签名
     * @param msg       原消息
     * @return 签名是否有效
     */
    @Override
    public boolean gVerify(GroupSignature signature, String msg) {
        // 取出签名信息
        Element c1 = g1.newElementFromBytes(signature.getC1Bytes());
        Element c2 = g1.newElementFromBytes(signature.getC2Bytes());
        Element w = g1.newElementFromBytes(signature.getWBytes());
        Element c = g1.newElementFromBytes(signature.getCBytes());

        // 获取系统公开参数
        Element d = this.gpk.getD();
        Element s = this.gpk.getS();

        // 计算各个映射项 Q = (e(c2, s) * e(p1, p2) ^ w) / e(c * c1 + d, p2)
        Element reflectC2S = this.pair.pairing(c2, s);
        Element p1MulW = p1.duplicate().mul(w);
        Element reflectP1P2 = this.pair.pairing(p1MulW, p2);
        Element cMulC1AddD = c.duplicate().mul(c1).add(d);
        Element denominator = this.pair.pairing(cMulC1AddD, p2);

        // 计算Q并计算Hash判断是否有效
        Element calculatedQ = reflectC2S.mul(reflectP1P2).div(denominator);
        Element calculatedQonG1 = g1.newElementFromBytes(calculatedQ.toBytes());
        String mDigest = CryptoUtil.applySm3Hash(msg);
        Element m = g1.newElementFromBytes(
                zr.newElementFromBytes(mDigest.getBytes(StandardCharsets.UTF_8)).toBytes()
        );
        Element calculatedHash = m.add(c1).add(c2).add(calculatedQonG1);

        return calculatedHash.isEqual(c);
    }

    /**
     * @param msg       消息
     * @param signature {@link GroupSignature}签名信息
     * @param ok        管理员打开签名私钥ok
     * @return 返回真实的交易发起方姓名
     */
    @Override
    public String gOpen(String msg, GroupSignature signature, AdminGroupOpenKey ok) {
        if (gVerify(signature, msg)) {
            Element c1 = signature.getC1();
            Element c2 = signature.getC2();
            Element uMulC1 = ok.getU().mul(c1);
            Element id = c2.sub(uMulC1);
            return this.joinedUserHashMap.get(id);
        } else {
            // 无效签名
            return null;
        }
    }
}
