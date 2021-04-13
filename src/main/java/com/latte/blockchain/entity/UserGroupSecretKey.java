package com.latte.blockchain.entity;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;

/**
 * @author float311
 * @since 2021/04/13
 */
public class UserGroupSecretKey {

    /**
     * x
     */
    @Getter
    private Element x;

    /**
     * Z
     */
    @Getter
    private Element z;

    public UserGroupSecretKey(Element x, Element z) {
        this.x = x;
        this.z = z;
    }
}
