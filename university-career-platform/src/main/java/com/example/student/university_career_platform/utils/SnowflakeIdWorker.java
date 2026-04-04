package com.example.student.university_career_platform.utils;

import lombok.Getter;

/**
 * 🌟 Twitter 雪花算法 (Snowflake)
 * 用于生成分布式下高并发的全局唯一有序 ID
 */
public class SnowflakeIdWorker {
    @Getter
    private static final SnowflakeIdWorker instance = new SnowflakeIdWorker(1, 1);

    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdWorker(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) { throw new RuntimeException("系统时钟回退，拒绝生成 ID"); }
        long sequenceBits = 12L;
        if (lastTimestamp == timestamp) {
            long sequenceMask = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) { timestamp = tilNextMillis(lastTimestamp); }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        long twepoch = 1420041600000L;
        long datacenterIdShift = sequenceBits + workerIdBits;
        long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << sequenceBits) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) { timestamp = timeGen(); }
        return timestamp;
    }
    private long timeGen() { return System.currentTimeMillis(); }
}
