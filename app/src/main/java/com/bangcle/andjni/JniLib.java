package com.bangcle.andjni;

import java.lang.reflect.Method;

/**
 * @author 云闪付
 */
@SuppressWarnings("JavaJniMissingFunction")
public class JniLib {
    public static native byte cB(Object... objArr);

    public static native char cC(Object... objArr);

    public static native double cD(Object... objArr);

    public static native float cF(Object... objArr);

    public static native int cI(Object... objArr);

    public static native long cJ(Object... objArr);

    public static native Object cL(Object... objArr);

    public static native short cS(Object... objArr);

    public static native void cV(Object... objArr);

    public static native boolean cZ(Object... objArr);

    static {
        try {
            System.loadLibrary("dexjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static Object InvokeObject(Object... args) throws Exception {
        int i = 0;
        Object thisObj = args[0];
        Method method = (Method) args[1];
        Object[] newArgs = new Object[(args.length - 2)];
        while (i < args.length - 2) {
            newArgs[i] = args[i + 2];
            i++;
        }
        return method.invoke(thisObj, newArgs);
    }
}