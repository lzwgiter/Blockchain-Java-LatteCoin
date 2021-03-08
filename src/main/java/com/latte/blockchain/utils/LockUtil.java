package com.latte.blockchain.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author float311
 * @since 2021/02/23
 */
public class LockUtil {
    private static final LockUtil LOCK_UTIL = new LockUtil();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantLock requestLock = new ReentrantLock();

    private final Condition condition = requestLock.newCondition();


    private LockUtil() {}

    public static LockUtil getLockUtil() {
        return LOCK_UTIL;
    }

    public ReentrantReadWriteLock getReadWriteLock() {
        return lock;
    }

    public ReentrantLock getRequestLock() {
        return requestLock;
    }

    public Condition getCondition() {
        return condition;
    }
}
