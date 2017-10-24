//
// Created by slomka.jin on 2016/11/15.
//

#ifndef WIRELESSFAX_TIFFPAGERREPLACE_H
#define WIRELESSFAX_TIFFPAGERREPLACE_H

#endif //WIRELESSFAX_TIFFPAGERREPLACE_H
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <tiffio.h>

/* Header for class org_beyka_tiffbitmapfactory_TiffSaver */

#define LOGI(x) __android_log_print(ANDROID_LOG_DEBUG, "NativeTiffSaver", "%s", x)
#define LOGII(x, y) __android_log_print(ANDROID_LOG_DEBUG, "NativeTiffSaver", "%s %d", x, y)
#define LOGIS(x, y) __android_log_print(ANDROID_LOG_DEBUG, "NativeTiffSaver", "%s %s", x, y)

#define LOGE(x) __android_log_print(ANDROID_LOG_ERROR, "NativeTiffSaver", "%s", x)
#define LOGES(x, y) __android_log_print(ANDROID_LOG_ERROR, "NativeTiffSaver", "%s %s", x, y)

#ifndef _Included_org_beyka_tiffbitmapfactory_TiffReplace
#define _Included_org_beyka_tiffbitmapfactory_TiffReplace
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_beyka_tiffbitmapfactory_TiffSaver
 * Method:    save
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_org_beyka_tiffbitmapfactory_TiffReplace_replace
        (JNIEnv *env, jclass clazz, jstring filePath, jstring destFilePath, jstring resultPath,jint index);

#ifdef __cplusplus
}
#endif
#endif