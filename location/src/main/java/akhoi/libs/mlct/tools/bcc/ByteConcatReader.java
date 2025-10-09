package akhoi.libs.mlct.tools.bcc;

public class ByteConcatReader {
    private final byte[] content;
    private int position;
    private int posPartial;

    public ByteConcatReader(byte[] content) {
        this.content = content;
    }

    public int readInt(int size) {
        int remaining = Math.clamp(size, 0, 32);
        if (remaining == 0) {
            return 0;
        }

        int remainder = 8 - posPartial;
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
            int msbCount = size - 32;
            long msbs = readInt(msbCount) & ((1L << msbCount) - 1);
            long lsbs = readInt(32) & 0xFFFFFFFFL;
            return (msbs << 32) | lsbs;
        }
    }

    public double readDouble(int size) {
        long longValue = readLong(size);
        return Double.longBitsToDouble(longValue);
    }

    public void reset() {
        posPartial = 0;
    }

    public void skip(int count) {
        posPartial += count;
    }

    private int readByte(int value, int size, int remainder) {
        if (position >= content.length) {
            return value;
        }

        int read = ((1 << size) - 1) & (content[position] >>> (remainder - size));
        int posNextByte = posPartial + size;
        position += posNextByte >>> 3;
        posPartial = posNextByte & 7;
        return (value << size) | read;
    }
}
