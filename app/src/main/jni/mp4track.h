//
// Created by YinJ on 26/06/2017.
//

#ifndef MP4V2DEMO_MP4TRACK_H
#define MP4V2DEMO_MP4TRACK_H

#include <stdlib.h>

enum {
    MP4V2_RET_SUCC = 0,
    MP4V2_RET_FAIL = -1,
};

typedef struct
{
    MP4FileHandle       pv_handle;
    uint32_t            ui_trackid;
    uint32_t            ui_samples;
    uint32_t            ui_maxsample_size;
    uint32_t            ui_w;
    uint32_t            ui_h;
    uint32_t            ui_framerate;
    uint32_t            ui_sps_size;
    uint8_t             auc_sps[64];
    uint32_t            ui_pps_size;
    uint8_t             auc_pps[64];
} mp4_video_info_t;

typedef struct
{
    MP4FileHandle       pv_handle;
    uint32_t            ui_trackid;
    uint32_t            ui_channels;
    uint32_t            ui_timescale;
    uint32_t            ui_duration;
    uint32_t            ui_bitrate;
} mp4_audio_info_t;

typedef struct
{
    char                ac_filename[64];
    mp4_video_info_t    st_video_info;
    mp4_audio_info_t    st_audio_info;
} mp4_track_info_t;

#endif //MP4V2DEMO_MP4TRACK_H
