package com.latte.blockchain.entity;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;

/**
 * 管理员群私钥
 *
 * @author float311
 * @since 2021/04/13
 */
public class AdminGroupSecretKey {
    /**
     * d
     */
    @Getter
    private Element d;

    /**
     * s
     */
    @Getter
    private Element s;

    public AdminGroupSecretKey(Element d, Element s) {
        this.d = d;
        this.s = s;
    }
}
