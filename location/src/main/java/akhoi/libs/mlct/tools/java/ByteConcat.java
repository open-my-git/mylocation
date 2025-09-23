package akhoi.libs.mlct.tools.java;

import androidx.annotation.VisibleForTesting;

import java.util.Arrays;

public class ByteConcat {
    private final Object lock = new Object();
    private byte[] bucket;
    private int byteIndex;
    private int position;

    public ByteConcat() {
        this(2);
    }

    public ByteConcat(int initCap) {
        int highest = Integer.highestOneBit(initCap);
        bucket = new byte[highest];
    }

    public byte[] getContent() {
        int length = byteIndex + ((position + 7) >>> 3);
        return Arrays.copyOf(bucket, length);
    }

    public void appendInt(int value, int size) {
        int actualSize = Math.max(Math.min(size, 32), 0);
        if (actualSize == 0) {
            return;
        }
        actualSize = appendHighestByte(value, actualSize, 8 - position);
        actualSize = appendHighestByte(value, actualSize, 8);
        actualSize = appendHighestByte(value, actualSize, 8);
        actualSize = appendHighestByte(value, actualSize, 8);
        appendHighestByte(value, actualSize, 8);
    }

    public void appendLong(long value, int size) {
        if (size <= 32) {
            appendInt((int) (value & 0xFFFFFFFFL), size);
        } else {
            appendInt((int) (value >>> 32), size - 32);
            appendInt((int) (value & 0xFFFFFFFFL), 32);
        }
    }

    private int appendHighestByte(int value, int size, int remainder) {
        if (size <= 0) {
            return size;
        }
        synchronized (lock) {
            if (byteIndex == bucket.length) {
                int capableSize = Math.max(bucket.length << 1, bucket.length | (bucket.length >>> 1));
                if (capableSize < bucket.length) {
                    throw new OutOfMemoryError("Bucket size overflow, current: " + bucket.length + ", next: " + capableSize);
                }
                bucket = Arrays.copyOf(bucket, capableSize);
            }
            int shifted = (value << (32 - size)) >>> (32 - remainder);
            bucket[byteIndex] = (byte) (bucket[byteIndex] | shifted);
            int nextBytePosition = position + Math.min(size, remainder);
            byteIndex += nextBytePosition >>> 3;
            position = nextBytePosition & 7;
        }
        return size - remainder;
    }

    @VisibleForTesting
    public int getBucketSize() {
        return bucket.length;
    }
}
