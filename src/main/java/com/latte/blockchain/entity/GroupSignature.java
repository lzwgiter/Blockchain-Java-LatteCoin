package com.latte.blockchain.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;
import lombok.Setter;

/**
 * 群签名定义
 *
 * @author float311
 * @since 2021/04/13
 */
public class GroupSignature {
    /**
     * C1
     */
    @Getter
    @Setter
    @JsonIgnore
    private Element c1;

    @Getter
    private byte[] c1Bytes;

    /**
     * C2
     */
    @Getter
    @Setter
    @JsonIgnore
    private Element c2;

    @Getter
    private byte[] c2Bytes;

    /**
     * c - 哈希值
     */
    @Getter
    @Setter
    @JsonIgnore
    private Element c;

    @Getter
    @JsonProperty(value = "cbytes")
    private byte[] cBytes;

    /**
     * w
     */
    @Getter
    @Setter
    @JsonIgnore
    private Element w;

    @Getter
    @JsonProperty(value = "wbytes")
    private byte[] wBytes;

    public GroupSignature() {}

    public GroupSignature(Element c1, Element c2, Element c, Element w) {
        this.c1 = c1;
        this.c2 = c2;
        this.c = c;
        this.w = w;
        this.c1Bytes = c1.toBytes();
        this.c2Bytes = c2.toBytes();
        this.cBytes = c.toBytes();
        this.wBytes = w.toBytes();
    }
}
