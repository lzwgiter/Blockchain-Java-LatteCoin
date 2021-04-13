package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

/**
 * 群签名公钥
 *
 * @author float311
 * @since 2021/04/13
 */
@Data
public class GroupPublicKey {
    /**
     * D
     */
    @JsonIgnore
    private Element d;

    /**
     * S
     */
    @JsonIgnore
    private Element s;

    /**
     * U
     */
    @JsonIgnore
    private Element u;

    public GroupPublicKey(Element d, Element s, Element u) {
        this.d = d;
        this.s = s;
        this.u = u;
    }
}
