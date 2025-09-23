package akhoi.libs.mlct.tools.java;

public class ByteConcatReader {
    private final byte[] content;
    private int byteIndex;
    private int position;

    public ByteConcatReader(byte[] content) {
        this.content = content;
    }

    public int readInt(int size) {
        int remaining = Math.max(Math.min(size, 32), 0);
        if (remaining == 0) {
            return 0;
        }
        int remainder = 8 - position;
        int readSize = Math.min(remaining, remainder);
        int result = readByte(0, readSize, remainder);
        remaining -= readSize;
        if (remaining <= 0) {
            return result;
        }
        readSize = Math.min(remaining, 8);
        result = readByte(result, readSize, 8);
        remaining -= readSize;
        if (remaining <= 0) {
            return result;
        }
        readSize = Math.min(remaining, 8);
        result = readByte(result, readSize, 8);
        remaining -= readSize;
        if (remaining <= 0) {
            return result;
        }
        readSize = Math.min(remaining, 8);
        result = readByte(result, readSize, 8);
        remaining -= readSize;
        if (remaining <= 0) {
            return result;
        }
        readSize = Math.min(remaining, 8);
        return readByte(result, readSize, 8);
    }

    public long readLong(int size) {
        if (size <= 32) {
            long value = readInt(size);
            return value & ((1L << size) - 1);
        } else {
            int msBitsCount = size - 32;
            long msBits = readInt(msBitsCount) & ((1L << msBitsCount) - 1);
            long lsBits = readInt(32) & 0xFFFFFFFFL;
            return (msBits << 32) | lsBits;
        }
    }

    public double readDouble(int size) {
        long longValue = readLong(size);
        return Double.longBitsToDouble(longValue);
    }

    public void reset() {
        position = 0;
    }

    public void skip(int count) {
        position += count;
    }

    private int readByte(int value, int size, int remainder) {
        if (byteIndex >= content.length) {
            return value;
        }
        int read = ((1 << size) - 1) & ((content[byteIndex] & 0xFF) >>> (remainder - size));
        int nextBytePosition = position + size;
        byteIndex += nextBytePosition >>> 3;
        position = nextBytePosition & 7;
        return (value << size) | read;
    }
}
