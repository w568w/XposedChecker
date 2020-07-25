//
// Created by w568w on 2020/7/25.
//

#include "xposed_check.h"
#include <jni.h>
#include <cstdio>
#include <sys/types.h>
#include <dirent.h>
#include <unistd.h>
#include <cstdlib>
#include <string.h>
#include <android/log.h>

#define TAG "JNI"
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG,\
        "[%s][%s][%d]: " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);

jboolean isXposedMaps();

extern "C" JNIEXPORT jboolean

JNICALL
Java_ml_w568w_checkxposed_util_NativeDetect_detectXposed(
        JNIEnv *env,
        jclass clazz) {

    return isXposedMaps();
}

long filelength(FILE *fp) {
    long num;
    fseek(fp, 0, SEEK_END);
    num = ftell(fp);
    fseek(fp, 0, SEEK_SET);
    return num;
}

jboolean isXposedMaps() {
    FILE *maps;
    char *path = "/proc/self/maps";
    char *content;
    if ((maps = fopen(path, "r")) == nullptr) {
        return false;
    } else {
        int len = filelength(maps);
        content = (char *) malloc(len);
        fread(content, len, 1, maps);
        content[len - 1] = '\0';
        //LOGD("dssd");
        return strstr(content, "XposedBridge") != nullptr;
    }
}