package akhoi.libs.mlct.tools.java;

import androidx.annotation.VisibleForTesting;

import java.util.Arrays;

public class ByteConcat {
    private static final int MIN_CAPACITY = 2;
    private final Object contentLock = new Object();
    private byte[] content;
    private int position;
    private int posPartial;

    public ByteConcat() {
        this(MIN_CAPACITY);
    }

    public ByteConcat(int initCap) {
        int capacity = Integer.highestOneBit(Math.max(initCap, MIN_CAPACITY));
        content = new byte[capacity];
    }

    public byte[] getContent() {
        int length = position + ((posPartial + 7) >>> 3);
        return Arrays.copyOf(content, length);
    }

    public void appendInt(int value, int size) {
        int actualSize = Math.max(Math.min(size, 32), 0);
        if (actualSize == 0) {
            return;
        }
        actualSize = appendHighestByte(value, actualSize, 8 - posPartial);
        actualSize = appendHighestByte(value, actualSize, 8);
        actualSize = appendHighestByte(value, actualSize, 8);
        actualSize = appendHighestByte(value, actualSize, 8);
        appendHighestByte(value, actualSize, 8);
    }

    public void appendLong(long value, int size) {
        if (size <= 32) {
            appendInt((int) value, size);
        } else {
            appendInt((int) (value >>> 32), size - 32);
            appendInt((int) value, 32);
        }
    }

    private int appendHighestByte(int value, int size, int remainder) {
        if (size <= 0) {
            return size;
        }
        synchronized (contentLock) {
            if (position == content.length) {
                int capableSize = Math.max(content.length << 1, content.length | (content.length >> 1));
                if (capableSize < content.length) {
                    throw new OutOfMemoryError("Bucket size overflow, current: " + content.length + ", next: " + capableSize);
                }
                content = Arrays.copyOf(content, capableSize);
            }
            content[position] = (byte) (content[position] | (value << (32 - size)) >>> (32 - remainder));
            int nextBytePos = posPartial + Math.min(size, remainder);
            position += nextBytePos >>> 3;
            posPartial = nextBytePos & 7;
        }
        return size - remainder;
    }

    @VisibleForTesting
    public int getContentSize() {
        return content.length;
    }
}
