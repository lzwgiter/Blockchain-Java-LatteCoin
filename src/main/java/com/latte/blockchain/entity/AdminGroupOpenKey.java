package com.latte.blockchain.entity;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;

/**
 * 管理员群签名打开私钥
 *
 * @author float311
 * @since 2021/04/13
 */
public class AdminGroupOpenKey {
    /**
     * u
     */
    @Getter
    private final Element u;

    public AdminGroupOpenKey(Element u) {
        this.u = u;
    }
}
