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
#include <dlfcn.h>

#define TAG "JNI"
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG,\
        "[%s][%d]: " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG,\
        "[%s][%d]: " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);

jboolean isXposedMaps();

static jboolean is_zygote_methods_replaced(JNIEnv *env, jobject thiz);

static jint get_riru_rersion(JNIEnv *env, jobject thiz);

extern "C" {
#include "pmparser.h"
}
procmaps_iterator *maps;
extern "C" JNIEXPORT jboolean
JNICALL
Java_ml_w568w_checkxposed_util_NativeDetect_detectXposed(

        JNIEnv *env,
        jclass clazz, jint pid) {
    maps = pmparser_parse(pid);
    if (maps == nullptr) {
        LOGE("[map]: cannot parse the memory map");
        return JNI_FALSE;
    }
    jboolean result =
            isXposedMaps() || is_zygote_methods_replaced(env, nullptr) || get_riru_rersion(env,
                                                                                           nullptr) !=
                                                                          -1;
    pmparser_free(maps);
    return result;
}

//long filelength(FILE *fp) {
//    long length = 0;
//    while (++length && fgetc(fp) != EOF);
//    rewind(fp);
//    return length;
//}

//Check if Riru or Xposed enabled.
//Mainly inspired by https://github.com/RikkaApps/Riru/blob/master/app/src/main/cpp/helper.cpp
jboolean isXposedMaps() {
    jboolean res = JNI_FALSE;
    procmaps_struct *maps_tmp = nullptr;
    while ((maps_tmp = pmparser_next(maps)) != nullptr) {
        LOGD("%s", maps_tmp->pathname);
        if (strstr(maps_tmp->pathname, "libmemtrack_real.so") ||
            strstr(maps_tmp->pathname, "XposedBridge")) {
            res = JNI_TRUE;
        }
    }

    return res;
}

static void *handle;

static void *get_handle() {
    if (handle == nullptr)
        handle = dlopen(nullptr, 0);

    return handle;
}

static jint get_riru_rersion(JNIEnv *env, jobject thiz) {
    static void *sym;
    void *handle;
    if ((handle = get_handle()) == nullptr) return -1;
    if (sym == nullptr) sym = dlsym(handle, "riru_get_version");
    if (sym) return ((int (*)()) sym)();
    return -1;
}

static jboolean is_zygote_methods_replaced(JNIEnv *env, jobject thiz) {
    static void *sym;
    void *handle;
    if ((handle = get_handle()) == nullptr) return JNI_FALSE;
    if (sym == nullptr) sym = dlsym(handle, "riru_is_zygote_methods_replaced");
    if (sym) return static_cast<jboolean>(((int (*)()) sym)());
    return JNI_FALSE;
}