LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE     := tremolo

LOCAL_SRC_FILES  := bitwise.c \
                    codebook.c \
                    dsp.c \
                    floor0.c \
                    floor1.c \
                    floor_lookup.c \
                    framing.c \
                    mapping0.c \
                    mdct.c \
                    misc.c \
                    res012.c \
                    treminfo.c \
                    vorbisfile.c \
                    hide.c \
                    tremolo-jni.c \
                    md5.c

ifeq ($(TARGET_ARCH),arm)
    LOCAL_ARM_MODE   := arm
    LOCAL_SRC_FILES  += bitwiseARM.s \
                        dpen.s \
                        floor1ARM.s \
                        mdctARM.s
endif

LOCAL_LDFLAGS += -Wl,--gc-sections

LOCAL_CFLAGS     := -ffast-math -O3 -fvisibility=hidden -ffunction-sections -fdata-sections

ifeq ($(TARGET_ARCH),arm)
  LOCAL_CFLAGS     += -D_ARM_ASSEM_
else
  LOCAL_CFLAGS     += -DONLY_C
endif

LOCAL_C_INCLUDES:= $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)