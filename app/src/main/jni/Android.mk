LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := mp4v2
LOCAL_SRC_FILES := $(LOCAL_PATH)/libs/libmp4v2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
TARGET_ARCH_ABI := armeabi
LOCAL_MODULE    := native-lib
LOCAL_CPPFLAGS     := -O0 -D_UNICODE -DUNICODE -DUSE_DUMP -Wno-error=format-security
LOCAL_CPP_EXTENSION := .cpp
LOCAL_LDLIBS    := -lm -llog -lz
LOCAL_SHORT_COMMANDS := true

INC_DIRS := -I$(LOCAL_PATH)/jni
INC_DIRS += -I$(LOCAL_PATH)/include
LOCAL_CPPFLAGS += $(INC_DIRS)

LOCAL_SRC_FILES    := CvrMP4v2.cpp


LOCAL_SHARED_LIBRARIES := mp4v2

include $(BUILD_SHARED_LIBRARY)