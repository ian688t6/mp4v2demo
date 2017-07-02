
#ifndef MP4V2DEMO_MP4V2CODEC_H
#define MP4V2DEMO_MP4V2CODEC_H

#include <jni.h>
#include <stdlib.h>
#include <pthread.h>

/** Get Video Info
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param MP4VideoInfo
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_get_video_info(JNIEnv *env, jobject object, jobject obj);

/** Read MP4 Video Track Sample
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param ByteBuffer bytebuffer
 *  @param jint Track id
 *  @param jint Sample id
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_read_video(JNIEnv *env, jobject object, jobject jbyte, jint trackid, jint sampleid);

/** Read MP4 Audio Track Sample
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param ByteBuffer bytebuffer
 *  @param jint Track id
 *  @param jint Sample id
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_read_audio(JNIEnv *env, jobject object, jbyteArray jba, jint trackid, jint off, jint cnt);

/** Open MP4 FIle
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param jstring filename
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_open_mp4file(JNIEnv *env, jobject object, jstring filename);

/** Close MP4 FIle
 *
 *  @param JNIEnv env
 *  @param jobject object
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_close_mp4file(JNIEnv *env, jobject object);


/** Get Track Sample Time
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param jint sampleid
 *
 *  @return jlong sample time
 */
JNIEXPORT jlong JNICALL native_get_sampletime(JNIEnv *env, jobject object, jint trackid, jint sampleid);

/** Get Audio Info
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param MP4AudioInfo object
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_get_audio_info(JNIEnv *env, jobject object, jobject obj);

JNIEXPORT jint JNICALL native_read_sample(JNIEnv *env, jobject object, jobject byteBuf, jint trackid, jint sampleid, jint cnt);

#endif //MP4V2DEMO_MP4V2CODEC_H
