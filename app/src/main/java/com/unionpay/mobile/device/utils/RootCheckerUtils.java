package com.unionpay.mobile.device.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.jrummyapps.android.shell.Shell;

import java.io.File;

/**
 * @author 云闪付开发者
 */
public class RootCheckerUtils {
    public static final String[] KNOWN_DANGEROUS_APPS_PACKAGES;
    public static final String[] KNOWN_ROOT_APPS_PACKAGES;
    public static final String[] KNOWN_ROOT_CLOAKING_PACKAGES;
    public static final String[] PATHS_THAT_SHOULD_NOT_BE_WRTIABLE;
    public static final String[] SU_PATHS = {"/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/", "/system/usr/we-need-root/", "/system/xbin/"};

    static {
        PATHS_THAT_SHOULD_NOT_BE_WRTIABLE = new String[]{"/system", "/system/bin", "/system/sbin", "/system/xbin", "/vendor/bin", "/sbin", "/etc"};
        KNOWN_DANGEROUS_APPS_PACKAGES = new String[]{"com.koushikdutta.rommanager", "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch", "com.ramdroid.appquarantine"};
        KNOWN_ROOT_CLOAKING_PACKAGES = new String[]{"com.devadvance.rootcloak", "de.robv.android.xposed.installer", "com.saurik.substrate", "com.devadvance.rootcloakplus", "com.zachspong.temprootremovejb", "com.amphoras.hidemyroot", "com.formyhm.hideroot"};
        KNOWN_ROOT_APPS_PACKAGES = new String[]{"com.noshufou.android.su", "com.noshufou.android.su.elite", "eu.chainfire.supersu", "com.koushikdutta.superuser", "com.thirdparty.superuser", "com.yellowes.su"};
    }

    private static boolean checkForBinary(String paramString) {
        int l = 0;
        String[] arrayOfString = SU_PATHS;
        int j = arrayOfString.length;
        int i = 0;
        while (i < j) {
            String str = arrayOfString[i];
            str = str + paramString;
            int k = l;
            if (new File(str).exists()) {
                k = 1;
            }
            ++i;
            l = k;
        }
        return l != 0;
    }


    private static boolean isAnyPackageFromListInstalled(Context paramContext, String[] paramList) {
        PackageManager pm = paramContext.getPackageManager();
        for (String str2 : paramList) {
            try {
                pm.getPackageInfo(str2, 0);
                return true;
            } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
                localNameNotFoundException.printStackTrace();
            }

        }
        return false;
    }


    public static boolean detect(Context context) {
        return checkForBinary("su")
                || isAnyPackageFromListInstalled(context, KNOWN_ROOT_APPS_PACKAGES)
                || isAnyPackageFromListInstalled(context, KNOWN_DANGEROUS_APPS_PACKAGES)
                || isAnyPackageFromListInstalled(context, KNOWN_ROOT_CLOAKING_PACKAGES)
                || Shell.SU.available();
    }

}