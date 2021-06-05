package com.latte.blockchain.service.impl;

import com.latte.blockchain.entity.*;
import com.latte.blockchain.utils.CryptoUtil;
import com.latte.blockchain.service.IGsService;

import java.io.ByteArrayOutputStream;
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

    private final HashMap<String, String> joinedUserHashMap = new HashMap<>();

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
        // d,s,u <- Zr
        Element adminD = this.zr.newRandomElement();
        Element adminS = this.zr.newRandomElement();
        Element adminU = this.zr.newRandomElement();

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
        Element adminD = adminWallet.getAsk().getD();
        Element adminS = adminWallet.getAsk().getS();
        Element adminU = adminWallet.getOk().getU();

        // 计算生成系统参数
        Element sysD = this.p1.duplicate().mulZn(adminD);
        Element sysS = this.p2.duplicate().mulZn(adminS);
        Element sysU = this.p1.duplicate().mulZn(adminU);

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
        Element x = this.zr.newRandomElement();

        // 获取管理员参数
        Element adminD = sk.getD();
        Element adminS = sk.getS();

        // 计算z = (d - x) * (s * x)^-1 * p1
        Element dSubX = adminD.duplicate().sub(x);
        Element invertsMulX = adminS.duplicate().mul(x).invert();
        Element z = this.p1.duplicate().mulZn(dSubX.mul(invertsMulX));

        // 赋予群用户
        newUser.setGsk(new UserGroupSecretKey(x, z));
        // 添加到群中，并添加唯一用户表示 x * z
        Element xMulZ = z.duplicate().mulZn(x);
        String id = CryptoUtil.applySm3Hash(new String(xMulZ.toBytes()));
        this.joinedUserHashMap.put(id, newUser.getName());
        log.info("[Initiation] 用户" + newUser.getName() + "已加入群！");
    }

    /**
     * 为一个消息进行群签名
     *
     * @param msg 消息
     * @param gsk {@link UserGroupSecretKey} 用户群私钥
     * @return {@link GroupSignature} 签名信息
     */
    @Override
    public GroupSignature gSign(String msg, UserGroupSecretKey gsk) {
        // 随机从整数群中选择一个k
        Element k = this.zr.newRandomElement();
        Element x = gsk.getX();
        Element z = gsk.getZ();

        // 系统参数
        Element sysU = this.gpk.getU();
        Element sysS = this.gpk.getS();

        // 计算C1, C2以及映射
        // c1 = k * p1;
        Element c1 = this.p1.duplicate().mulZn(k);

        // c2 = x * Z + k * U
        Element xMulZ = z.duplicate().mulZn(x);
        Element kMulU = sysU.duplicate().mulZn(k);
        Element c2 = xMulZ.duplicate().add(kMulU);

        // 计算映射: e(U, S)^k
        Element q = pair.pairing(sysU, sysS).powZn(k);

        // 计算Hash: c = hash(M, C1, C2, Q)
        String mDigest = CryptoUtil.applySm3Hash(msg);
        Element c = zr.newElementFromBytes(applySecurityHash(q, c1, c2, mDigest));

        // 计算w = k * c + x
        Element w = c.duplicate().mul(k).add(x);

        return new GroupSignature(c1, c2, c, w);
    }

    /**
     * @param signature {@link GroupSignature}待验证的签名
     * @param msg       原消息
     * @return 签名是否有效
     */
    @Override
    public boolean gVerify(GroupSignature signature, String msg) {
        // 取出并恢复签名信息
        Element c1 = g1.newElementFromBytes(signature.getC1Bytes());
        Element c2 = g1.newElementFromBytes(signature.getC2Bytes());
        Element w = zr.newElementFromBytes(signature.getWBytes());
        Element c = zr.newElementFromBytes(signature.getCBytes());
        signature.setC1(c1);
        signature.setC2(c2);
        signature.setW(w);
        signature.setC(c);

        // 获取系统公开参数
        Element sysD = this.gpk.getD();
        Element sysS = this.gpk.getS();

        // 计算各个映射项 Q = (e(c2, S) * e(p1, p2) ^ w) / e(c * c1 + D, p2)
        Element reflectC2S = this.pair.pairing(c2, sysS);
        Element reflectP1P2 = this.pair.pairing(p1, p2).powZn(w);
        Element cMulC1AddD = c1.duplicate().mulZn(c).add(sysD);
        Element denominator = this.pair.pairing(cMulC1AddD, p2);

        // 计算Q并计算Hash判断是否有效
        Element calculatedQ = reflectC2S.mul(reflectP1P2).div(denominator);
        String mDigest = CryptoUtil.applySm3Hash(msg);
        Element calculatedHash = zr.newElementFromBytes(
                applySecurityHash(calculatedQ, c1, c2, mDigest));

        if (calculatedHash.isEqual(c)) {
            log.info("[Processing Transaction] 签名验证成功，交易有效");
            return true;
        } else {
            log.info("[Processing Transaction] 签名验证失败，交易无效！");
            return false;
        }
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
            Element uMulC1 = c1.duplicate().mulZn(ok.getU());
            String id = CryptoUtil.applySm3Hash(
                    new String(c2.duplicate().sub(uMulC1).toBytes()));
            if (this.joinedUserHashMap.containsKey(id)) {
                log.info("[Tracing Transaction] 群签名打开成功！打开人：admin");
                return this.joinedUserHashMap.get(id);
            } else {
                return null;
            }

        } else {
            // 无效签名
            return null;
        }
    }

    /**
     * 计算签名中的哈希部分
     *
     * @param q  Q
     * @param c1 C1
     * @param c2 C2
     * @param m  message
     * @return byte[]
     */
    private byte[] applySecurityHash(Element q, Element c1, Element c2, String m) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(q.toBytes());
            bos.write(c1.toBytes());
            bos.write(c2.toBytes());
            bos.write(m.getBytes());
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // error
        return null;
    }
}
