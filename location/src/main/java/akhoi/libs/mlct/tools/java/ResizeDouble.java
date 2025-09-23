package akhoi.libs.mlct.tools.java;

public final class ResizeDouble {
    private ResizeDouble() {
    }

    public static long compressDouble(double number, int desExpSize, int desSigSize) {
        return akhoi.libs.mlct.tools.ResizeDoubleKt.compressDouble(number, desExpSize, desSigSize);
    }

    public static double expandDouble(long source, int srcExpSize, int srcSigSize) {
        return akhoi.libs.mlct.tools.ResizeDoubleKt.expandDouble(source, srcExpSize, srcSigSize);
    }

    public static long resizeDouble(long src) {
        return akhoi.libs.mlct.tools.ResizeDoubleKt.resizeDouble(src, 11, 52, 11, 52);
    }

    public static long resizeDouble(long src, int srcExpSize, int srcSigSize) {
        return akhoi.libs.mlct.tools.ResizeDoubleKt.resizeDouble(src, srcExpSize, srcSigSize, 11, 52);
    }

    public static long resizeDouble(long src, int srcExpSize, int srcSigSize, int desExpSize, int desSigSize) {
        return akhoi.libs.mlct.tools.ResizeDoubleKt.resizeDouble(src, srcExpSize, srcSigSize, desExpSize, desSigSize);
    }
}
