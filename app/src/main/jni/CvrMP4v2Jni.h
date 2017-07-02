#ifndef MP4V2DEMO_JNI_H
#define MP4V2DEMO_JNI_H



#include <jni.h>

#include "CvrMP4Class.h"

JNINativeMethod g_nativeMethod[] = {
        /* 获取 mp4 video info */
        { "getMP4VideoInfo",    "(Lcom/gc/mp4v2demo/MP4VideoInfo;)I",                    (void*)native_get_video_info   },

        /* 获取audio info */
        { "getMP4AudioInfo",    "(Lcom/gc/mp4v2demo/MP4AudioInfo;)I",                    (void *)native_get_audio_info  },

        /* 读取 mp4 sample */
        { "readSample",         "(Ljava/nio/ByteBuffer;III)I",                           (void *)native_read_sample     },

        /* 读取 mp4 video sample */
        { "readVideoSample",    "(Ljava/nio/ByteBuffer;II)I",                            (void *)native_read_video      },

        /* 读取 mp4 audio sample */
        { "readAudioSample",    "([BIII)I",                                              (void *)native_read_audio      },

        /* 打开 mp4 file */
        { "openMP4File",        "(Ljava/lang/String;)I",                                 (void *)native_open_mp4file    },

        /* 关闭 mp4 file */
        { "closeMP4File",       "()I",                                                   (void *)native_close_mp4file   },

        /* 获取 采样时间 */
        { "getSampleTime",      "(II)J",                                                 (void *)native_get_sampletime  },
};

/*
 * 被虚拟机自动调用
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    jclass jClass = env->FindClass(JNI_MP4V2CODEC);
    env->RegisterNatives(jClass,g_nativeMethod, sizeof(g_nativeMethod) / sizeof(g_nativeMethod[0]));
    env->DeleteLocalRef(jClass);
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv *env;
    int nJNIVersionOK = vm->GetEnv((void **)&env, JNI_VERSION_1_6) ;
    jclass jClass = env->FindClass(JNI_MP4V2CODEC);
    env->UnregisterNatives(jClass);
    env->DeleteLocalRef(jClass);
}

#endif
