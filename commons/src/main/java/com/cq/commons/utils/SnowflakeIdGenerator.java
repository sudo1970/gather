package com.cq.commons.utils;

import java.util.concurrent.atomic.AtomicLong;

public class SnowflakeIdGenerator {
    private final long epoch = 1288834974657L; // 起始时间戳，可以根据实际需要设定
    private final AtomicLong lastTimestamp = new AtomicLong(0);
    private final AtomicLong sequence = new AtomicLong(0);
    private final long workerId; // 工作机器ID
    private final long dataCenterId; // 数据中心ID
    private final long sequenceMask = -1L ^ (-1L << 12); // 序列号掩码
    private final long workerIdShift = 12L; // 工作机器ID向左移12位
    private final long dataCenterIdShift = 12L + 5L; // 数据中心ID向左移17位
    private final long timestampLeftShift = 22L + 5L + 5L; // 时间戳向左移27位

    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * nextId方法首先获取当前时间戳，如果发现时间戳小于上次ID生成时的时间戳，
     * 则等待直到时间戳增加。这样可以确保时间戳的单调递增，从而避免了时钟回拨问题。
     * 如果时间戳正常，方法会生成新的ID并返回。如果时间戳小于上次的时间戳，
     * 则会递归调用nextId来生成新的ID
     * @return
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        while (timestamp < lastTimestamp.get()) {
            try {
                // 如果时间戳小于上次ID生成的时间戳，则等待直到时间戳增加
                wait(1000);
                timestamp = timeGen();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (lastTimestamp.compareAndSet(lastTimestamp.get(), timestamp)) {
            long sequenceVal = sequence.getAndIncrement() & sequenceMask;
            if (sequenceVal == 0) {
                timestamp = tilNextMillis(lastTimestamp.get());
            }
            return (timestamp - epoch) << timestampLeftShift | workerId << workerIdShift | dataCenterId << dataCenterIdShift | sequenceVal;
        } else {
            return nextId(); // 递归调用
        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
