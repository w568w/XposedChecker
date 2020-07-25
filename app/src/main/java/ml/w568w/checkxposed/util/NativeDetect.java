package ml.w568w.checkxposed.util;

public class NativeDetect {
    static {
        System.loadLibrary("xposed_check");
    }

    public static native boolean detectXposed();
}
