package com.unionpay.mobile.device.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.bangcle.andjni.JniLib;
import com.jrummyapps.android.shell.Shell;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author 云闪付
 */
public class RootCheckerUtils {
    private static final String TAG = RootCheckerUtils.class.getSimpleName();
    public static final String[] knownDangerousAppsPackages = new String[]{"com.koushikdutta.rommanager", "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch", "com.ramdroid.appquarantine"};
    public static final String[] knownRootAppsPackages = new String[]{"com.noshufou.android.su", "com.noshufou.android.su.elite", "eu.chainfire.supersu", "com.koushikdutta.superuser", "com.thirdparty.superuser", "com.yellowes.su"};
    public static final String[] knownRootCloakingPackages = new String[]{"com.yaerin.xposed.hide", "com.devadvance.rootcloak", "de.robv.android.xposed.installer", "com.saurik.substrate", "com.devadvance.rootcloakplus", "com.zachspong.temprootremovejb", "com.amphoras.hidemyroot", "com.formyhm.hideroot"};
    public static final String[] pathsThatShouldNotBeWrtiable = new String[]{"/system", "/system/bin", "/system/sbin", "/system/xbin", "/vendor/bin", "/sbin", "/etc"};
    public static final String[] suPaths = new String[]{"/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/", "/system/usr/we-need-root/", "/system/xbin/"};

    private static boolean checkForBusyBoxBinary() {
        return JniLib.cZ(5932);
    }

    private static boolean checkForSuBinary() {
        return JniLib.cZ(5933);
    }

    private static boolean checkSuExists() {
        return JniLib.cZ(5934);
    }

    private boolean detectPotentiallyDangerousApps(Context context) {
        return JniLib.cZ(this, context, 5935);
    }

    private boolean detectPotentiallyDangerousApps(Context context, String[] strArr) {
        return JniLib.cZ(this, context, strArr, 5936);
    }

    private boolean detectRootCloakingApps(Context context) {
        return JniLib.cZ(this, context, 5937);
    }

    private static boolean detectRootCloakingApps(Context context, String[] strArr) {
        return JniLib.cZ(context, strArr, 5938);
    }

    private boolean detectRootManagementApps(Context context) {
        return JniLib.cZ(this, context, 5939);
    }

    private boolean detectRootManagementApps(Context context, String[] strArr) {
        return JniLib.cZ(this, context, strArr, 5940);
    }

    private static boolean isExecutable(String str) {
        return JniLib.cZ(str, 5941);
    }

    public static boolean isRoot() {
        return JniLib.cZ(5942);
    }

    public static boolean isRooted(Context context) {
        return JniLib.cZ(context, 5943);
    }

    private static String[] mountReader() {
        return (String[]) JniLib.cL(5944);
    }

    private static String[] propsReader() {
        return (String[]) JniLib.cL(5945);
    }

    private boolean detectTestKeys() {
        String str = Build.TAGS;
        return str != null && str.contains("test-keys");
    }

    private static boolean checkForBinary(String str) {
        String[] strArr = suPaths;
        try {
            int length = strArr.length;
            int i = 0;
            boolean z = false;
            while (i < length) {
                try {
                    String str2 = strArr[i] + str;
                    if (new File(str2).exists() && isExecutable(str2)) {
                        z = true;
                    }
                    i++;
                } catch (Exception e) {
                    return z;
                }
            }
            return z;
        } catch (Exception e2) {
            return false;
        }
    }

    private static boolean isAnyPackageFromListInstalled(Context context, List<String> list) {
        PackageManager packageManager = context.getPackageManager();
        boolean z = false;
        for (String str : list) {
            try {
                packageManager.getPackageInfo(str, 0);
                z = true;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return z;
    }

    private static boolean checkForDangerousProps() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("ro.debuggable", "1");
        hashMap.put("ro.secure", "0");
        try {
            String[] propsReader = propsReader();
            if (propsReader == null) {
                return false;
            }
            int length = propsReader.length;
            int i = 0;
            while (i < length) {
                try {
                    String str = propsReader[i];
                    Iterator<String> it = hashMap.keySet().iterator();
                    while (true) {
                        try {
                            if (!it.hasNext()) {
                                break;
                            }
                            String str2 = it.next();
                            if (str.contains(str2)) {
                                String str3 = "[" + hashMap.get(str2) + "]";
                                if (str.contains(str3)) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    i++;
                } catch (Exception e2) {
                    return false;
                }
            }
            return false;
        } catch (Exception e3) {
            return false;
        }
    }

    private static boolean checkForRWPaths() {
        boolean z = false;
        try {
            String[] mountReader = mountReader();
            if (mountReader == null) {
                return false;
            }
            for (String str : mountReader) {
                String[] split = str.split(" ");
                if (split.length >= 4) {
                    String str2 = split[1];
                    String str3 = split[3];
                    for (String str4 : pathsThatShouldNotBeWrtiable) {
                        if (str2.equalsIgnoreCase(str4)) {
                            for (String equalsIgnoreCase : str3.split(",")) {
                                if (equalsIgnoreCase.equalsIgnoreCase("rw")) {
                                    z = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean detect(Context context) {
        return checkForBinary("su") || checkForBusyBoxBinary() || checkForDangerousProps() || checkForRWPaths() || checkForSuBinary() || checkSuExists()
                || isAnyPackageFromListInstalled(context, Arrays.asList(knownRootAppsPackages))
                || isAnyPackageFromListInstalled(context, Arrays.asList(knownDangerousAppsPackages))
                || isAnyPackageFromListInstalled(context, Arrays.asList(knownRootCloakingPackages))
                || Shell.SU.available();
    }

}