#include <string.h>
#include <jni.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "ivorbiscodec.h"
#include "ivorbisfile.h"

struct JniParams {
    JNIEnv *env;
    jobject obj;
    void *jniBuffer;
    jmethodID writeOgg;
    jmethodID seekOgg;
    jmethodID tellOgg;
    int current_section;
};

static size_t oggRead(void *ptr, size_t size, size_t nmemb, void *datasource){
    struct JniParams *params = datasource;
    int written = (*params->env)->CallIntMethod(params->env, params->obj, params->writeOgg, 1 * nmemb);
    if(written > 0){
        memcpy(ptr, params->jniBuffer, written);
    }

    return written;
}

static int oggSeek(void *datasource, ogg_int64_t offset, int whence)
{
    struct JniParams *params = datasource;

    int result = (*params->env)->CallIntMethod(params->env, params->obj, params->seekOgg, offset, whence);

    return result;
}

static long oggTell(void *datasource)
{
    struct JniParams *params = datasource;
    int position = (*params->env)->CallIntMethod(params->env, params->obj, params->tellOgg);
    return position;
}

JNIEXPORT jlong JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_initDecoder(JNIEnv *env, jobject obj, jobject jJjniBuffer)
{
    void *jniBuffer = (*env)->GetDirectBufferAddress(env, jJjniBuffer);

    jclass cls = (*env)->GetObjectClass(env, obj);

    jmethodID writeOgg = (*env)->GetMethodID(env, cls, "writeOgg", "(I)I");
    jmethodID seekOgg = (*env)->GetMethodID(env, cls, "seekOgg", "(JI)I");
    jmethodID tellOgg = (*env)->GetMethodID(env, cls, "tellOgg", "()I");

    struct JniParams *params;
    params = (struct JniParams *)malloc(sizeof(struct JniParams));
    params->env = env;
    params->jniBuffer = jniBuffer;
    params->writeOgg = writeOgg;
    params->seekOgg = seekOgg;
    params->tellOgg = tellOgg;
    params->obj = obj;

    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)malloc(sizeof(struct OggVorbis_File));

    ov_callbacks callbacks = {
        (size_t (*)(void *, size_t, size_t, void *))  oggRead,
        (int (*)(void *, ogg_int64_t, int))           oggSeek, //noseek
        (int (*)(void *))                             fclose,
        (long (*)(void *))                            oggTell // notell
    };

    if(ov_open_callbacks(params, vf, NULL, 0, callbacks) < 0) {
        // Input does not appear to be an Ogg bitstream
        return 0;
    }

    jlong pointer = (jlong) vf;
    return pointer;
}

static unsigned int samplesWritten = 0;

static unsigned int pcm_bytes_to_frames(unsigned int bytes) {
	return bytes / (2 * (16 >> 3));
}

JNIEXPORT jlong JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_read(JNIEnv *env, jobject obj, jlong jHandle, jint jLen)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;

    return ov_read(vf,params->jniBuffer,jLen,&params->current_section);
}

JNIEXPORT jint JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_seekMs(JNIEnv *env, jobject obj, jlong jHandle, jint jMilliseconds)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;
    return ov_time_seek(vf, jMilliseconds);
}

JNIEXPORT jint JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_seekSamples(JNIEnv *env, jobject obj, jlong jHandle, jint jSamples)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;
    return ov_pcm_seek(vf, jSamples);
}



JNIEXPORT jlong JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_tellMs(JNIEnv *env, jobject obj, jlong jHandle)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;
    return ov_time_tell(vf);
}

JNIEXPORT jlong JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_tellSamples(JNIEnv *env, jobject obj, jlong jHandle)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;
    return ov_pcm_tell(vf);
}

JNIEXPORT jlong JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_totalSamples(JNIEnv *env, jobject obj, jlong jHandle)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;

    struct JniParams *params = vf->datasource;
    params->env = env;
    params->obj = obj;
    return ov_pcm_total(vf,-1);
}

JNIEXPORT void JNICALL Java_xyz_gianlu_librespot_player_codecs_tremolo_OggDecodingInputStream_close(JNIEnv *env, jobject obj, jlong jHandle)
{
    struct OggVorbis_File *vf;
    vf = (struct OggVorbis_File *)jHandle;
    struct JniParams *params = vf->datasource;

    free(params);
    vf->datasource = NULL;
    ov_clear(vf);
    free(vf);
    vf = NULL;
}