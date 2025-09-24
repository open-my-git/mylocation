package akhoi.libs.mlct.tools.java;

public final class ResizeDouble {
    private ResizeDouble() {
    }

    public static long compressDouble(double number, int desExpSize, int desSigSize) {
        long bits = Double.doubleToRawLongBits(number);
        return resizeDouble(bits, 11, 52, desExpSize, desSigSize);
    }

    public static double expandDouble(long source, int srcExpSize, int srcSigSize) {
        long resized = resizeDouble(source, srcExpSize, srcSigSize, 11, 52);
        return Double.longBitsToDouble(resized);
    }

    public static long resizeDouble(long src) {
        return resizeDouble(src, 11, 52, 11, 52);
    }

    public static long resizeDouble(long src, int srcExpSize, int srcSigSize) {
        return resizeDouble(src, srcExpSize, srcSigSize, 11, 52);
    }

    public static long resizeDouble(long src, int srcExpSize, int srcSigSize, int desExpSize, int desSigSize) {
        int srcActualExpSize = clamp(srcExpSize, 0, 11);
        int srcActualSigSize = clamp(srcSigSize, 1, 52);
        int desActualExpSize = clamp(desExpSize, 0, 11);
        int desActualSigSize = clamp(desSigSize, 1, 52);

        long desExpMask = (desActualExpSize == 0) ? 0L : (1L << desActualExpSize) - 1L;
        long desBias = desExpMask >> 1;
        long desExponent;
        if (desActualExpSize == 0) {
            desExponent = 0L;
        } else if (srcActualExpSize == 0) {
            desExponent = (1L + desBias) & desExpMask;
        } else {
            long srcExpMask = (1L << srcActualExpSize) - 1L;
            long srcBias = srcExpMask >> 1;
            long srcExponent = (src >> srcActualSigSize) & srcExpMask;
            if (srcExponent == 0L) {
                desExponent = 0L;
            } else if (srcExponent == srcExpMask) {
                desExponent = desExpMask;
            } else {
                desExponent = (srcExponent - srcBias + desBias) & desExpMask;
            }
        }

        long srcSignificandMask = (1L << srcActualSigSize) - 1L;
        long desSignificandMask = (1L << desActualSigSize) - 1L;
        long desSignificand = src & srcSignificandMask;
        int shiftLeft = Math.max(desActualSigSize - srcActualSigSize, 0);
        int shiftRight = Math.max(srcActualSigSize - desActualSigSize, 0);
        if (shiftLeft > 0) {
            desSignificand <<= shiftLeft;
        }
        if (shiftRight > 0) {
            desSignificand >>>= shiftRight;
        }
        desSignificand &= desSignificandMask;

        long desSign = (src >> (srcActualExpSize + srcActualSigSize)) & 1L;
        return (desSign << desActualExpSize) | (desExponent << desActualSigSize) | desSignificand;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
