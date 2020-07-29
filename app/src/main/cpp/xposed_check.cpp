#pragma clang diagnostic push
#pragma ide diagnostic ignored "hicpp-deprecated-headers"
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
#include <sys/stat.h>
#include <fcntl.h>

#define TAG "JNI"
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG,\
        "[%s][%d]: " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG,\
        "[%s][%d]: " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);

jboolean isXposedMaps(jint pid);

extern "C" JNIEXPORT jboolean

JNICALL
Java_ml_w568w_checkxposed_util_NativeDetect_detectXposed(

        JNIEnv *env,
        jclass clazz, jint pid) {
    return isXposedMaps(pid);
}

long filelength(FILE *fp) {
//    long num;
//    int result=fseek(fp, 0, SEEK_END);
//    LOGD("Result code:%d",result);
//    num = ftell(fp);
//    fseek(fp, 0, SEEK_SET);
//    return num;
    long length = 0;
    while (++length && fgetc(fp) != EOF);
    rewind(fp);
    return length;
}

jboolean isXposedMaps(jint pid) {
    FILE *maps;
    char path[20];
    sprintf(path, "/proc/%d/maps", pid);
    char *content;
    LOGD("File path:%s", path);
    if ((maps = fopen(path, "rb")) == nullptr) {
        LOGE("Can't read maps!");
        return false;
    } else {
        int len = filelength(maps);
        LOGD("File length:%d", len);
        content = (char *) malloc(len + 1);
        fread(content, len, 1, maps);
        content[len] = '\0';
        LOGD("Length=%d,\n%s", strlen(content), content);
        return strstr(content, "XposedBridge") != nullptr;
    }
}

#pragma clang diagnostic pop