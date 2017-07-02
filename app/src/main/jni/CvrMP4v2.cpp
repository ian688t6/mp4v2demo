
#include "CvrMP4v2.h"
#include "CvrMP4v2Jni.h"
#include "NLog.h"
#include "mp4v2.h"
#include "mp4track.h"

static mp4_track_info_t     g_st_trackinfo;

static void mp4_dump_video_info(mp4_video_info_t *pst_info)
{
    uint32_t ui_i = 0;

    if (NULL == pst_info)
        return ;

    NLogi("video info trackid: %d w: %d h: %d framerate: %d samples: %d sps_len: %d pps_len: %d",
          pst_info->ui_trackid, pst_info->ui_w, pst_info->ui_h,
          pst_info->ui_framerate, pst_info->ui_samples,
          pst_info->ui_sps_size , pst_info->ui_pps_size);

    NLogi("SPS:");
    for (ui_i = 0; ui_i < pst_info->ui_sps_size; ui_i += 8) {
        NLogi("%02x %02x %02x %02x %02x %02x %02x %02x",
              pst_info->auc_sps[ui_i + 0], pst_info->auc_sps[ui_i + 1], pst_info->auc_sps[ui_i + 2], pst_info->auc_sps[ui_i + 3],
              pst_info->auc_sps[ui_i + 4], pst_info->auc_sps[ui_i + 5], pst_info->auc_sps[ui_i + 6], pst_info->auc_sps[ui_i + 7]);
    }

    NLogi("PPS:");
    for (ui_i = 0; ui_i < pst_info->ui_pps_size; ui_i += 8) {
        NLogi("%02x %02x %02x %02x %02x %02x %02x %02x",
              pst_info->auc_pps[ui_i + 0], pst_info->auc_pps[ui_i + 1], pst_info->auc_pps[ui_i + 2], pst_info->auc_pps[ui_i + 3],
              pst_info->auc_pps[ui_i + 4], pst_info->auc_pps[ui_i + 5], pst_info->auc_pps[ui_i + 6], pst_info->auc_pps[ui_i + 7]);
    }

    return ;
}

static void mp4_get_video_info(MP4FileHandle handle, uint32_t trackid, mp4_video_info_t *pst_info)
{
    uint32_t ui_i = 0;
    uint8_t  **ppuc_sps     = NULL;
    uint8_t  **ppuc_pps     = NULL;
    uint32_t *pui_spslen    = NULL;
    uint32_t *pui_ppslen    = NULL;

    if (NULL == pst_info)
    {
        NLoge("video info null");
        return ;
    }
    pst_info->ui_w = MP4GetTrackVideoWidth(handle, trackid);
    pst_info->ui_h = MP4GetTrackVideoHeight(handle, trackid);
    pst_info->ui_trackid    = trackid;
    pst_info->ui_framerate  = MP4GetTrackVideoFrameRate(handle, trackid);
    pst_info->ui_samples    = MP4GetTrackNumberOfSamples(handle, trackid);
    pst_info->ui_maxsample_size = MP4GetTrackMaxSampleSize(handle, trackid);
    MP4GetTrackH264SeqPictHeaders(handle, trackid, &ppuc_sps, &pui_spslen, &ppuc_pps, &pui_ppslen);
    /* sps */
    for (ui_i = 0; 0 != pui_spslen[ui_i]; ui_i ++)
    {
        memcpy(pst_info->auc_sps, ppuc_sps[ui_i], pui_spslen[ui_i]);
        pst_info->ui_sps_size = pui_spslen[ui_i];
        free(ppuc_sps[ui_i]);
    }
    free(ppuc_sps);
    free(pui_ppslen);

    /* pps */
    for (ui_i = 0; 0 != pui_ppslen[ui_i]; ui_i ++)
    {
        memcpy(pst_info->auc_pps, ppuc_pps[ui_i], pui_ppslen[ui_i]);
        pst_info->ui_pps_size = pui_ppslen[ui_i];
        free(ppuc_pps[ui_i]);
    }
    free(ppuc_pps);
    free(pui_ppslen);

    /* dump */
    mp4_dump_video_info(pst_info);

    return ;
}

static void mp4_get_audio_info(MP4FileHandle handle, uint32_t ui_trackid, mp4_audio_info_t *pst_info)
{
    uint32_t ui_samples = 0;

    if (NULL == pst_info)
    {
        NLoge("audio info null");
        return ;
    }

    pst_info->ui_trackid    = ui_trackid;
    pst_info->ui_channels   = (uint32_t)MP4GetTrackAudioChannels(handle, ui_trackid);
    pst_info->ui_timescale  = MP4GetTrackTimeScale(handle, ui_trackid);
    pst_info->ui_bitrate    = MP4GetTrackBitRate(handle, ui_trackid);
    pst_info->ui_duration   = (uint32_t)MP4GetTrackDuration(handle, ui_trackid);
    ui_samples = MP4GetTrackNumberOfSamples(handle, ui_trackid);

    NLogi("audeo trackid: %d channels: %d timescale: %d bitrate: %d duration: %d samples :%d",
        pst_info->ui_trackid, pst_info->ui_channels, pst_info->ui_timescale,
        pst_info->ui_bitrate, pst_info->ui_duration, ui_samples);

    return ;
}

static int mp4_read_video_sample(mp4_video_info_t *pst_info, uint8_t *puc_src, uint8_t *puc_dst, uint32_t ui_size, bool is_sync)
{
    uint8_t auc_nal[4]  = {0x00, 0x00, 0x00, 0x01};
    uint32_t ui_len     = ui_size;

    if ((NULL == pst_info) || (NULL == puc_src) || (NULL == puc_dst))
    {
        NLoge("parameter is invalid");
        return MP4V2_RET_FAIL;
    }

    if (is_sync)
    {
        memcpy(puc_dst, auc_nal, sizeof(auc_nal));
        puc_dst += sizeof(auc_nal);
        ui_len += sizeof(auc_nal);

        memcpy(puc_dst, g_st_trackinfo.st_video_info.auc_sps, g_st_trackinfo.st_video_info.ui_sps_size);
        puc_dst += g_st_trackinfo.st_video_info.ui_sps_size;
        ui_len += g_st_trackinfo.st_video_info.ui_sps_size;

        memcpy(puc_dst, auc_nal, sizeof(auc_nal));
        puc_dst += sizeof(auc_nal);
        ui_len += sizeof(auc_nal);

        memcpy(puc_dst, g_st_trackinfo.st_video_info.auc_pps, g_st_trackinfo.st_video_info.ui_pps_size);
        puc_dst += g_st_trackinfo.st_video_info.ui_pps_size;
        ui_len += g_st_trackinfo.st_video_info.ui_pps_size;
    }

    if (ui_size > 4)
    {
        puc_src[0] = 0x00;
        puc_src[1] = 0x00;
        puc_src[2] = 0x00;
        puc_src[3] = 0x01;
        memcpy(puc_dst, puc_src, ui_size);
    }

    return ui_len;
}

static int mp4_read_audio(MP4FileHandle pv_handle, void *pv_dst, uint32_t ui_trackid, uint32_t off, uint32_t cnt)
{
    uint8_t     *pbuf       = NULL;
    uint8_t     *puc_dst    = NULL;
    uint32_t    ui_size     = 0;
    uint32_t    ui_len      = 0;
    uint32_t    ui_sampleid = 1;
    MP4Timestamp    ul_timestamp    = 0;
    MP4Duration     ul_duration     = 0;
    MP4Duration     ul_rendering    = 0;
    bool            is_syncsample   = false;


    puc_dst = (uint8_t *)pv_dst;
    for (ui_sampleid = off; ui_sampleid < (off + cnt); ui_sampleid ++)
    {
        if (!MP4ReadSample(pv_handle, ui_trackid, ui_sampleid, &pbuf, &ui_size, &ul_timestamp, &ul_duration, &ul_rendering, &is_syncsample))
        {
            NLogw("MP4ReadSample ui_len %d", ui_len);
            return ui_len;
        }
        puc_dst[0] = pbuf[0];
        puc_dst[1] = pbuf[1];
        puc_dst += ui_size;
        ui_len += ui_size;
        free(pbuf);
    }

    return ui_len;
}

static int mp4_read_video(MP4FileHandle pv_handle, void *pv_dst, uint32_t ui_trackid, uint32_t ui_sampleid)
{
    uint8_t     *pbuf       = NULL;
    uint8_t     *puc_dst    = NULL;
    uint32_t    ui_size     = 0;
    uint32_t    ui_len      = 0;
    MP4Timestamp    ul_timestamp    = 0;
    MP4Duration     ul_duration     = 0;
    MP4Duration     ul_rendering    = 0;
    bool            is_syncsample   = false;

    if (!MP4ReadSample(pv_handle, ui_trackid, ui_sampleid, &pbuf, &ui_size, &ul_timestamp, &ul_duration, &ul_rendering, &is_syncsample)) {
        NLoge("Fail ReadSample: %d size: %d", ui_sampleid, ui_size);
        return MP4V2_RET_FAIL;
    }

    puc_dst = (uint8_t *)pv_dst;
    /* read video track sample */
    ui_len = (uint32_t)mp4_read_video_sample(&g_st_trackinfo.st_video_info, pbuf, puc_dst, ui_size, is_syncsample);

    free(pbuf);
    return ui_len;
}

static int mp4_get_track_info(MP4FileHandle pv_fhandle, mp4_track_info_t *pst_info)
{
    uint32_t        ui_trackid      = MP4_INVALID_TRACK_ID;

    if (NULL == pv_fhandle)
    {
        NLoge("Mp4FileHandle not open");
        return MP4V2_RET_FAIL;
    }

    for (ui_trackid = 1; ui_trackid <= MP4GetNumberOfTracks(pv_fhandle); ui_trackid ++)
    {
        const char *pc_tracktype = MP4GetTrackType(pv_fhandle, ui_trackid);
        NLogi("%s: %s", pc_tracktype, MP4Info(pv_fhandle, ui_trackid));

        if (NULL == pc_tracktype)
        {
            continue;
        }

        if (MP4_IS_VIDEO_TRACK_TYPE(pc_tracktype)) {
            /* video track */
            mp4_get_video_info(pv_fhandle, ui_trackid, &pst_info->st_video_info);
        } else if (MP4_IS_AUDIO_TRACK_TYPE(pc_tracktype)){
            /* default audio track */
            mp4_get_audio_info(pv_fhandle, ui_trackid, &pst_info->st_audio_info);
        } else {
            NLogw("%s", MP4Info(pv_fhandle, ui_trackid));
        }

    }

    return MP4V2_RET_SUCC;
}

/** Get Video Info
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param MP4VideoInfo
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_get_video_info(JNIEnv *env, jobject object, jobject jobj)
{
    jfieldID        fd;
    jbyteArray      jarr;
    jbyte           *jbelem;

    if (!MP4_IS_VALID_TRACK_ID(g_st_trackinfo.st_video_info.ui_trackid))
    {
        NLoge("Invalid track id");
        return MP4V2_RET_FAIL;
    }

    jclass jcInfo = env->FindClass(JNI_MP4V2INFO);
    fd = env->GetFieldID(jcInfo, "mTrackid", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_trackid);

    fd = env->GetFieldID(jcInfo, "mWidth", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_w);

    fd = env->GetFieldID(jcInfo, "mHeight", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_h);

    fd = env->GetFieldID(jcInfo, "mSamples", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_samples);

    fd = env->GetFieldID(jcInfo, "mSampleMaxsize", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_maxsample_size);

    fd = env->GetFieldID(jcInfo, "mFramerate", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_video_info.ui_framerate);

    fd = env->GetFieldID(jcInfo, "mSpsHeader", "[B");
    jarr = env->NewByteArray(g_st_trackinfo.st_video_info.ui_sps_size);
    jbelem = env->GetByteArrayElements(jarr, 0);
    memcpy(jbelem, g_st_trackinfo.st_video_info.auc_sps, g_st_trackinfo.st_video_info.ui_sps_size);
    env->SetByteArrayRegion(jarr, 0, g_st_trackinfo.st_video_info.ui_sps_size, jbelem);
    env->SetObjectField(jobj, fd, jarr);

    fd = env->GetFieldID(jcInfo, "mPpsHeader", "[B");
    jarr = env->NewByteArray(g_st_trackinfo.st_video_info.ui_pps_size);
    jbelem = env->GetByteArrayElements(jarr, 0);
    memcpy(jbelem, g_st_trackinfo.st_video_info.auc_pps, g_st_trackinfo.st_video_info.ui_pps_size);
    env->SetByteArrayRegion(jarr, 0, g_st_trackinfo.st_video_info.ui_pps_size, jbelem);
    env->SetObjectField(jobj, fd, jarr);

    return MP4V2_RET_SUCC;
}

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
JNIEXPORT jint JNICALL native_read_audio(JNIEnv *env, jobject object, jbyteArray jba, jint trackid, jint off, jint cnt)
{
    uint8_t     *pbuf       = NULL;
    jbyte       *jelem      = NULL;
    int8_t     *pc_dst      = NULL;
    uint32_t    ui_size     = 0;
    uint32_t    ui_len      = 0;
    uint32_t    ui_sampleid = 1;
    MP4Timestamp    ul_timestamp    = 0;
    MP4Duration     ul_duration     = 0;
    MP4Duration     ul_rendering    = 0;
    bool            is_syncsample   = false;

    jelem = env->GetByteArrayElements(jba, NULL);
    pc_dst = jelem;
    if ((NULL == pc_dst) || (NULL == g_st_trackinfo.st_audio_info.pv_handle)) {
        NLoge("jba null or pv handle null");
        return MP4V2_RET_FAIL;
    }

    for (ui_sampleid = (uint32_t)off; ui_sampleid < (off + cnt); ui_sampleid ++)
    {
        if (!MP4ReadSample(g_st_trackinfo.st_audio_info.pv_handle, trackid, ui_sampleid, &pbuf, &ui_size, &ul_timestamp, &ul_duration, &ul_rendering, &is_syncsample))
        {
            NLogw("MP4ReadSample ui_len %d", ui_len);
            return ui_len;
        }

        memcpy(pc_dst, pbuf, ui_size);
        pc_dst += ui_size;
        ui_len += ui_size;
    }
    env->SetByteArrayRegion(jba, 0, ui_len, jelem);

    return ui_len;
}

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
JNIEXPORT jint JNICALL native_read_video(JNIEnv *env, jobject object, jobject byteBuf, jint trackid, jint sampleid)
{
    void        *dst        = env->GetDirectBufferAddress(byteBuf);
    uint8_t     *pbuf       = NULL;
    uint8_t     *puc_dst    = NULL;
    uint32_t    ui_size     = 0;
    uint32_t    ui_len      = 0;
    MP4Timestamp    ul_timestamp    = 0;
    MP4Duration     ul_duration     = 0;
    MP4Duration     ul_rendering    = 0;
    bool            is_syncsample   = false;

    if ((NULL == dst))
    {
        NLoge("You didn't open MP4FileHandle!");
        return MP4V2_RET_FAIL;
    }

    if (NULL == g_st_trackinfo.st_video_info.pv_handle)
    {
        NLogi("read video filename: %s trackid: %d sampleid: %d", g_st_trackinfo.ac_filename, trackid, sampleid);
        return MP4V2_RET_FAIL;
    }

//    NLogi("3 read trackid %d sampleid : %d", trackid, sampleid);
    if (!MP4ReadSample(g_st_trackinfo.st_video_info.pv_handle, trackid, sampleid, &pbuf, &ui_size, &ul_timestamp, &ul_duration, &ul_rendering, &is_syncsample)) {
        NLoge("Fail ReadSample: %d size: %d", sampleid, ui_size);
        return MP4V2_RET_FAIL;
    }

    puc_dst = (uint8_t *)dst;
    /* read video track sample */
    ui_len = (uint32_t)mp4_read_video_sample(&g_st_trackinfo.st_video_info, pbuf, puc_dst, ui_size, is_syncsample);

    free(pbuf);
    return ui_len;
}

/** Read MP4  Track Sample
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param ByteBuffer bytebuffer
 *  @param jint Track id
 *  @param jint Sample id
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_read_sample(JNIEnv *env, jobject object, jobject byteBuf, jint trackid, jint sampleid, jint cnt)
{
    uint32_t ui_len = 0;
    MP4FileHandle pv_handle = NULL;
    void *dst = env->GetDirectBufferAddress(byteBuf);

    if ((NULL == dst))
    {
        NLoge("You didn't open MP4FileHandle!");
        return MP4V2_RET_FAIL;
    }

    pv_handle = MP4Read(g_st_trackinfo.ac_filename);

    if (MP4_IS_VIDEO_TRACK_TYPE(MP4GetTrackType(pv_handle, (MP4TrackId)trackid))) {
        ui_len = (uint32_t)mp4_read_video(pv_handle, dst, (uint32_t)trackid, (uint32_t)sampleid);
    } else if (MP4_IS_AUDIO_TRACK_TYPE(MP4GetTrackType(pv_handle, (MP4TrackId)trackid))) {
        ui_len = (uint32_t)mp4_read_audio(pv_handle, dst, (uint32_t)trackid, (uint32_t)sampleid, (uint32_t)cnt);
    } else {
        NLogw("invalid trackid : %d", trackid);
    }
    MP4Close(pv_handle);

    return ui_len;
}


/** Open MP4 FIle
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param jstring filename
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_open_mp4file(JNIEnv *env, jobject object, jstring filename)
{
    char    *pc_filename    = (char *)env->GetStringUTFChars(filename, 0);

    if (NULL == pc_filename)
    {
        NLoge("Mp4 filename null");
        return MP4V2_RET_FAIL;
    }


    g_st_trackinfo.st_video_info.pv_handle = MP4Read(pc_filename);
    if (NULL == g_st_trackinfo.st_video_info.pv_handle)
    {
        NLoge("Mp4FileHandle null %s ", pc_filename);

        return MP4V2_RET_FAIL;
    }

    g_st_trackinfo.st_audio_info.pv_handle = MP4Read(pc_filename);
    if (NULL == g_st_trackinfo.st_audio_info.pv_handle)
    {
        NLoge("Mp4FileHandle null %s ", pc_filename);
        return MP4V2_RET_FAIL;
    }

    if (MP4V2_RET_SUCC != mp4_get_track_info(g_st_trackinfo.st_video_info.pv_handle, &g_st_trackinfo))
    {
        NLoge("Mp4 get track info fail");
        return MP4V2_RET_FAIL;
    }

    strncpy(g_st_trackinfo.ac_filename, pc_filename, sizeof(g_st_trackinfo.ac_filename));
    NLogi("4 open filename : %s", g_st_trackinfo.ac_filename);

    return MP4V2_RET_SUCC;
}

/** Close MP4 FIle
 *
 *  @param JNIEnv env
 *  @param jobject object
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_close_mp4file(JNIEnv *env, jobject object)
{
    if (NULL != g_st_trackinfo.st_video_info.pv_handle) {
        MP4Close(g_st_trackinfo.st_video_info.pv_handle);
        return MP4V2_RET_FAIL;
    }

    if (NULL != g_st_trackinfo.st_audio_info.pv_handle) {
        MP4Close(g_st_trackinfo.st_audio_info.pv_handle);
        return MP4V2_RET_FAIL;
    }

    memset(&g_st_trackinfo, 0x0, sizeof(mp4_track_info_t));

    return MP4V2_RET_SUCC;
}

/** Get Track Sample Time
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param jint sampleid
 *
 *  @return jlong sample time
 */
JNIEXPORT jlong JNICALL native_get_sampletime(JNIEnv *env, jobject object, jint trackid, jint sampleid)
{
    jlong ul_time;

    ul_time =  MP4GetSampleTime(g_st_trackinfo.st_video_info.pv_handle, trackid, sampleid);

    return ul_time;
}

/** Get Audio Info
 *
 *  @param JNIEnv env
 *  @param jobject object
 *  @param MP4AudioInfo object
 *
 *  @return MP4V2_RET_SUCC/MP4V2_RET_FAIL
 */
JNIEXPORT jint JNICALL native_get_audio_info(JNIEnv *env, jobject object, jobject jobj)
{
    jfieldID        fd;

    if (!MP4_IS_VALID_TRACK_ID(g_st_trackinfo.st_audio_info.ui_trackid)) {
        NLoge("Audio track id %d is invalid", g_st_trackinfo.st_audio_info.ui_trackid);
        return MP4V2_RET_FAIL;
    }

    jclass jcInfo = env->FindClass(JNI_MP4V2_AUDIO_INFO);
    fd = env->GetFieldID(jcInfo, "mTrackid", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_audio_info.ui_trackid);

    fd = env->GetFieldID(jcInfo, "mChannels", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_audio_info.ui_channels);

    fd = env->GetFieldID(jcInfo, "mTimescale", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_audio_info.ui_timescale);

    fd = env->GetFieldID(jcInfo, "mDuration", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_audio_info.ui_duration);

    fd = env->GetFieldID(jcInfo, "mBitrate", "I");
    env->SetIntField(jobj, fd, g_st_trackinfo.st_audio_info.ui_bitrate);

    return MP4V2_RET_SUCC;
}


