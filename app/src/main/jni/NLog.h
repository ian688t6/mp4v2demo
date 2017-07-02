//
// Created by YinJ on 21/06/2017.
//

#ifndef MP4V2DEMO_NLOG_H
#define MP4V2DEMO_NLOG_H

#include <android/log.h>
#include <string.h>

#ifdef NLOG_TAG
#undef NLOG_TAG
#endif

#define NLOG_TAG "MP42DEMO"

#ifdef _WIN32
#define COMPART '\\'
#else
#define COMPART '/'
#endif

static char* ConvertTOShortFileName(const char* pszFileName)
{
    char *pszPret = const_cast<char *>(pszFileName);
    char *pszNext = NULL;

    if(NULL == pszFileName)
    {
        return NULL;
    }

    if ((pszNext = strrchr(pszPret, COMPART)) != NULL)
    {
        pszPret = pszNext + 1;
    }

    return pszPret;
}

#define NLogv(fmt, ...) __android_log_print(ANDROID_LOG_VERBOSE, NLOG_TAG, "[%s:%s](%d): " fmt, ConvertTOShortFileName(__FILE__), __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define NLogd(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG,   NLOG_TAG, "[%s:%s](%d): " fmt, ConvertTOShortFileName(__FILE__), __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define NLogi(fmt, ...) __android_log_print(ANDROID_LOG_INFO,    NLOG_TAG, "[%s:%s](%d): " fmt, ConvertTOShortFileName(__FILE__), __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define NLogw(fmt, ...) __android_log_print(ANDROID_LOG_WARN,    NLOG_TAG, "[%s:%s](%d): " fmt, ConvertTOShortFileName(__FILE__), __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define NLoge(fmt, ...) __android_log_print(ANDROID_LOG_ERROR,   NLOG_TAG, "[%s:%s](%d): " fmt, ConvertTOShortFileName(__FILE__), __FUNCTION__, __LINE__, ##__VA_ARGS__)

#endif //MP4V2DEMO_NLOG_H
