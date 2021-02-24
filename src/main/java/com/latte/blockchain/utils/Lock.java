package com.latte.blockchain.utils;

/**
 * @author float311
 * @since 2021/02/23
 */
public class Lock {
    private static final Lock LOCK = new Lock();

    private Lock() {}

    public static Lock getLock() {
        return LOCK;
    }
}
