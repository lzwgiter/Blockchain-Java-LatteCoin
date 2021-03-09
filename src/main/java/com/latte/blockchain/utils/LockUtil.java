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

    /**
     * 线程读写锁
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 请求锁
     */
    private final ReentrantLock stateLock = new ReentrantLock();

    /**
     * 线程等待队列
     */
    private final Condition writeCondition = stateLock.newCondition();


    private LockUtil() {}

    public static LockUtil getLockUtil() {
        return LOCK_UTIL;
    }

    public ReentrantReadWriteLock getReadWriteLock() {
        return lock;
    }

    public ReentrantLock getStateLock() {
        return stateLock;
    }

    public Condition getWriteCondition() {
        return writeCondition;
    }
}
