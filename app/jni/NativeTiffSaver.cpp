//
// Created by beyka on 18.2.16.
//
using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

#include <math.h>
#include <android/bitmap.h>
#include "NativeTiffSaver.h"
#include "NativeExceptions.h"

int const colorMask = 0xFF;



JNIEXPORT jboolean JNICALL Java_org_beyka_tiffbitmapfactory_TiffSaver_save
        (JNIEnv *env, jclass clazz, jstring filePath, jintArray img, jobject options, jint img_width, jint img_height,jint threshold) {

    const char *strPath = NULL;
    strPath = env->GetStringUTFChars(filePath, 0);
    LOGIS("nativeTiffOpenForSave", strPath);

    //Get array of jint from jintArray
    jint *c_array;
    c_array = env->GetIntArrayElements(img, NULL);
    if (c_array == NULL) {
        //if array is null - nothing to save
        LOGE("array is null");
        return JNI_FALSE;
    }

    //Get options
    jclass jSaveOptionsClass = env->FindClass(
            "org/beyka/tiffbitmapfactory/TiffSaver$SaveOptions");
    //Get compression mode from options object
    jfieldID gOptions_CompressionModeFieldID = env->GetFieldID(jSaveOptionsClass,
                                                               "compressionMode",
                                                               "Lorg/beyka/tiffbitmapfactory/TiffSaver$CompressionMode;");
    jobject compressionMode = env->GetObjectField(options, gOptions_CompressionModeFieldID);

    jclass compressionModeClass = env->FindClass(
            "org/beyka/tiffbitmapfactory/TiffSaver$CompressionMode");
    jfieldID ordinalFieldID = env->GetFieldID(compressionModeClass, "ordinal", "I");
    jint compressionInt = env->GetIntField(compressionMode, ordinalFieldID);

    //Get image orientation from options object
    jfieldID gOptions_OrientationFieldID = env->GetFieldID(jSaveOptionsClass,
                                                           "orientation",
                                                           "Lorg/beyka/tiffbitmapfactory/TiffSaver$Orientation;");
    jobject orientation = env->GetObjectField(options, gOptions_OrientationFieldID);

    jclass orientationClass = env->FindClass(
            "org/beyka/tiffbitmapfactory/TiffSaver$Orientation");
    jfieldID orientationOrdinalFieldID = env->GetFieldID(orientationClass, "ordinal", "I");
    jint orientationInt = env->GetIntField(orientation, orientationOrdinalFieldID);

    //Get author field if exist
    jfieldID gOptions_authorFieldID = env->GetFieldID(jSaveOptionsClass, "author", "Ljava/lang/String;");
    jstring jAuthor = (jstring)env->GetObjectField(options, gOptions_authorFieldID);
    const char *authorString = NULL;
    if (jAuthor) {
        authorString = env->GetStringUTFChars(jAuthor, 0);
        LOGIS("Author: ", authorString);
    }

    //Get copyright field if exist
    jfieldID gOptions_copyrightFieldID = env->GetFieldID(jSaveOptionsClass, "copyright", "Ljava/lang/String;");
    jstring jCopyright = (jstring)env->GetObjectField(options, gOptions_copyrightFieldID);
    const char *copyrightString = NULL;
    if (jCopyright) {
        copyrightString = env->GetStringUTFChars(jCopyright, 0);
        LOGIS("Copyright: ", copyrightString);
    }

    int pixelsBufferSize = img_width * img_height;

    int* array = ( int *) malloc(sizeof( int) * pixelsBufferSize);
    if (!array) {
        throw_not_enought_memory_exception(env, sizeof(int) * pixelsBufferSize);
        return JNI_FALSE;
    }
    int index = 0;
    for (int i = 0; i < img_width; i++) {
           for (int j = 0; j < img_height; j++) {
               jint crPix = c_array[j * img_width + i];
                char alpha = colorMask & crPix >> 24;
                char red = colorMask & crPix >> 16;
                char green = colorMask & crPix >> 8;
                char blue = colorMask & crPix;

            crPix = (alpha << 24) | (blue << 16) | (green << 8) | (red);
            array[j * img_width + i] = crPix;
        }
    }



    TIFF *output_image;
    // Open the TIFF file
    if((output_image = TIFFOpen(strPath, "w")) == NULL){
        LOGE("Unable to write tif file");
        throw_no_such_file_exception(env, filePath);
        return JNI_FALSE;
    }

    LOGII("compressionInt: ", compressionInt);

    int ret;
    if (compressionInt == COMPRESSION_CCITTFAX3){
        TIFFSetField(output_image, TIFFTAG_IMAGEWIDTH, img_width);
        TIFFSetField(output_image, TIFFTAG_IMAGELENGTH, img_height);
        TIFFSetField(output_image, TIFFTAG_FAXMODE, FAXMODE_CLASSIC);
        TIFFSetField(output_image, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
        TIFFSetField(output_image, TIFFTAG_COMPRESSION, COMPRESSION_CCITTFAX3);
        TIFFSetField(output_image, TIFFTAG_GROUP3OPTIONS,0);
        TIFFSetField(output_image, TIFFTAG_SAMPLESPERPIXEL, 1);
        //TIFFSetField(output_image, TIFFTAG_FILLORDER, FILLORDER_MSB2LSB);
        TIFFSetField(output_image, TIFFTAG_FILLORDER, FILLORDER_LSB2MSB);
        //TIFFSetField(output_image, TIFFTAG_ORIENTATION, 1);
        TIFFSetField(output_image, TIFFTAG_ORIENTATION, orientationInt);
        //TIFFSetField(output_image, TIFFTAG_XRESOLUTION, (float) 72.0);
        //TIFFSetField(output_image, TIFFTAG_YRESOLUTION, (float) 72.0);
        TIFFSetField(output_image, TIFFTAG_RESOLUTIONUNIT, RESUNIT_INCH);
        TIFFSetField(output_image, TIFFTAG_BITSPERSAMPLE, 1);
        TIFFSetField(output_image, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_MINISWHITE);
        unsigned char *ptr = (unsigned char*)(&array[0]); // initialize pointer to the first byte of the image buffer
        unsigned char red, green, blue, gray, eightPixels;
        tsize_t bytesPerStrip = ceil(img_width/8.0);
        unsigned char *strip = (unsigned char *)_TIFFmalloc(bytesPerStrip);

        for (int y=0; y<img_height; y++) {
            for (int x=0; x<img_width; x++) {
                red = *ptr++; green = *ptr++; blue = *ptr++;
                ptr++; // discard fourth byte by advancing the pointer 1 more byte
                gray = .3 * red + .59 * green + .11 * blue; // http://answers.yahoo.com/question/index?qid=20100608031814AAeBHPU
                eightPixels = strip[x/8];
                eightPixels = eightPixels << 1;
                if (gray < threshold) eightPixels = eightPixels | 1; // black=1 in tiff image without TIFFTAG_PHOTOMETRIC header
                strip[x/8] = eightPixels;
            }
            ret=TIFFWriteScanline(output_image, strip, y, 0);
        }
        if (strip) _TIFFfree(strip);
        //TIFFSetField(output_image, TIFFTAG_IMAGEWIDTH, img_width);
        //TIFFSetField(output_image, TIFFTAG_IMAGELENGTH, img_height/4*3);
        //TIFFSetField(output_image, TIFFTAG_IMAGELENGTH, img_height);
    }
    else{
        //Write tiff tags for saveing
        TIFFSetField(output_image, TIFFTAG_IMAGEWIDTH, img_width);
        TIFFSetField(output_image, TIFFTAG_IMAGELENGTH, img_height);
        TIFFSetField(output_image, TIFFTAG_BITSPERSAMPLE, 8);
        TIFFSetField(output_image, TIFFTAG_SAMPLESPERPIXEL, 4);
        TIFFSetField(output_image, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
        TIFFSetField(output_image, TIFFTAG_COMPRESSION, compressionInt);
        TIFFSetField(output_image, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_RGB);
        TIFFSetField(output_image, TIFFTAG_ORIENTATION, orientationInt);
        TIFFSetField(output_image, TIFFTAG_SUBFILETYPE,FILETYPE_REDUCEDIMAGE);

        //Write additiona tags
        if (authorString) {
            TIFFSetField(output_image, TIFFTAG_ARTIST, authorString);
        }
        if (copyrightString) {
            TIFFSetField(output_image, TIFFTAG_COPYRIGHT, copyrightString);
        }

        // Write the information to the file
        ret = TIFFWriteEncodedStrip(output_image, 0, &array[0], img_width*img_height * 4);
        free (array);
    }
    // Close the file
    TIFFClose(output_image);
    //free temp array

    //Remove variables
    if (authorString) {
        env->ReleaseStringUTFChars(jAuthor, authorString);
    }
    if (copyrightString) {
        env->ReleaseStringUTFChars(jCopyright, copyrightString);
    }
    env->ReleaseStringUTFChars(filePath, strPath);
    env->ReleaseIntArrayElements(img, c_array, 0);


    if (ret == -1) return JNI_FALSE;
    return JNI_TRUE;
}

//append multiple images to tiff page mode in one file.
JNIEXPORT jboolean JNICALL Java_org_beyka_tiffbitmapfactory_TiffSaver_append
        (JNIEnv *env, jclass clazz, jstring filePath, jobject bitmap, jobject options,jboolean append) {

//img
    if (bitmap == NULL) {
            const char *message = "Bitmap is null\0";
            LOGE(message);
            return JNI_FALSE;
        }
        jclass jSaveOptionsClass = env->FindClass(
                    "org/beyka/tiffbitmapfactory/TiffSaver$SaveOptions");
        jfieldID gOptions_CompressionModeFieldID = env->GetFieldID(jSaveOptionsClass,
                                                                       "compressionMode",
                                                                       "Lorg/beyka/tiffbitmapfactory/TiffSaver$CompressionMode;");
            jobject compressionMode = env->GetObjectField(options, gOptions_CompressionModeFieldID);

            jclass compressionModeClass = env->FindClass(
                    "org/beyka/tiffbitmapfactory/TiffSaver$CompressionMode");
            jfieldID ordinalFieldID = env->GetFieldID(compressionModeClass, "ordinal", "I");
            jint compressionInt = env->GetIntField(compressionMode, ordinalFieldID);
        AndroidBitmapInfo  info;
                void* pixels;
                int ret;



                if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
                    LOGE("AndroidBitmap_getInfo() failed ! error=");
                    return JNI_FALSE;
                }

                if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
                        LOGE("AndroidBitmap_lockPixels() failed ! error=");
                        return JNI_FALSE;
                }

                uint32 img_width = info.width;
                uint32 img_height= info.height;
    const char *strPath = NULL;
            strPath = env->GetStringUTFChars(filePath, 0);
            LOGIS("nativeTiffOpenForSave", strPath);
            //Get options


            //Get image orientation from options object
                jfieldID gOptions_OrientationFieldID = env->GetFieldID(jSaveOptionsClass,
                                                                       "orientation",
                                                                       "Lorg/beyka/tiffbitmapfactory/TiffSaver$Orientation;");
                jobject orientation = env->GetObjectField(options, gOptions_OrientationFieldID);

                jclass orientationClass = env->FindClass(
                        "org/beyka/tiffbitmapfactory/TiffSaver$Orientation");
                jfieldID orientationOrdinalFieldID = env->GetFieldID(orientationClass, "ordinal", "I");
                jint orientationInt = env->GetIntField(orientation, orientationOrdinalFieldID);
            env->DeleteLocalRef(orientationClass);



            //Get author field if exist
            jfieldID gOptions_authorFieldID = env->GetFieldID(jSaveOptionsClass, "author", "Ljava/lang/String;");
            jstring jAuthor = (jstring)env->GetObjectField(options, gOptions_authorFieldID);
            const char *authorString = NULL;
            if (jAuthor) {
                authorString = env->GetStringUTFChars(jAuthor, 0);
                LOGIS("Author: ", authorString);
            }

            //Get copyright field if exist
            jfieldID gOptions_copyrightFieldID = env->GetFieldID(jSaveOptionsClass, "copyright", "Ljava/lang/String;");
            jstring jCopyright = (jstring)env->GetObjectField(options, gOptions_copyrightFieldID);
            const char *copyrightString = NULL;
            if (jCopyright) {
                copyrightString = env->GetStringUTFChars(jCopyright, 0);
                LOGIS("Copyright: ", copyrightString);
            }

            //Get image description field if exist
            jfieldID gOptions_imgDescrFieldID = env->GetFieldID(jSaveOptionsClass, "imageDescription", "Ljava/lang/String;");
            jstring jImgDescr = (jstring)env->GetObjectField(options, gOptions_imgDescrFieldID);
            const char *imgDescrString = NULL;
            if (jImgDescr) {
                imgDescrString = env->GetStringUTFChars(jImgDescr, 0);
                LOGIS("Image Description: ", imgDescrString);
            }



            //Get android version
            jclass build_class = env->FindClass("android/os/Build$VERSION");
            jfieldID releaseFieldID = env->GetStaticFieldID(build_class, "RELEASE", "Ljava/lang/String;");
            jstring jrelease = (jstring)env->GetStaticObjectField(build_class, releaseFieldID);
            const char *releaseString = NULL;
            if (jrelease) {
                releaseString = env->GetStringUTFChars(jrelease, 0);
                LOGIS("Release: ", releaseString);
            }
            char *fullReleaseName = concat("Android ", releaseString);
            LOGIS("Full Release: ", fullReleaseName);


            uint32 pixelsBufferSize = img_width * img_height;
            uint32* img = NULL;
            int tmpImgArrayCreated = 0;
            switch (info.format) {
                case ANDROID_BITMAP_FORMAT_RGBA_8888:
                {
                    LOGI("ANDROID_BITMAP_FORMAT_RGBA_8888");
                    img = (uint32*)pixels;
                    break;
                }
                case ANDROID_BITMAP_FORMAT_RGBA_4444:
                {
                    LOGI("ANDROID_BITMAP_FORMAT_RGBA_4444");
                    uint16_t* tmp4444 = (uint16_t*)pixels;
                    img = (uint32*) malloc(sizeof(uint32) * pixelsBufferSize);
                    for (int x = 0; x < img_width; x++) {
                        for (int y = 0; y < img_height; y++) {
                            uint16_t pix = tmp4444[y * img_width + x];
                            int alpha = colorMask & pix >> 16;
                            int red = colorMask & pix >> 8;
                            int green = colorMask & pix >> 4;
                            int blue = colorMask & pix;
                            uint32 crPix = (alpha << 24) | (blue << 16) | (green << 8) | (red);
                            img[y * img_width + x] = crPix;
                        }
                    }
                    tmpImgArrayCreated = 1;
                    break;
                }
                case ANDROID_BITMAP_FORMAT_RGB_565:
                {
                    LOGI("ANDROID_BITMAP_FORMAT_RGB_565");
                    uint16_t* tmp565 = (uint16_t*)pixels;
                    img = (uint32*) malloc(sizeof(uint32) * pixelsBufferSize);
                    for (int x = 0; x < img_width; x++) {
                        for (int y = 0; y < img_height; y++) {
                            uint16_t pix = tmp565[y * img_width + x];
                            unsigned char red = 0b11111 & pix >> 11;
                            unsigned char green = 0b111111 & pix >> 5;
                            unsigned char blue = 0b11111 & pix;
                            uint32 crPix = (blue << 3 << 16) | (green << 2 << 8) | (red<<3);
                            img[y * img_width + x] = crPix;
                        }
                    }
                    tmpImgArrayCreated = 1;
                    break;
                }
                case ANDROID_BITMAP_FORMAT_A_8:
                {
                    LOGI("ANDROID_BITMAP_FORMAT_A_8");
                    uint8_t* tmp8 = (uint8_t*)pixels;
                    img = (uint32*) malloc(sizeof(uint32) * pixelsBufferSize);
                    for (int x = 0; x < img_width; x++) {
                        for (int y = 0; y < img_height; y++) {
                            uint8_t pix = tmp8[y * img_width + x];
                            img[y * img_width + x] = pix << 24;
                        }
                    }
                    tmpImgArrayCreated = 1;
                    break;
                }
            }
            TIFF *output_image;
            int fileDescriptor = -1;

            // Open the TIFF file
            if (!append) {
                if((output_image = TIFFOpen(strPath, "w")) == NULL){
                    LOGE("can not open file. Trying file descriptor");
                    //if TIFFOpen returns null then try to open file from descriptor
                    int mode = O_RDWR | O_CREAT | O_TRUNC | 0;
                    fileDescriptor = open(strPath, mode, 0666);
                    if (fileDescriptor < 0) {
                        LOGE("Unable to create tif file descriptor");
                        throw_cant_open_file_exception(env, filePath);
                        return JNI_FALSE;
                    } else {
                        if ((output_image = TIFFFdOpen(fileDescriptor, strPath, "w")) == NULL) {
                            close(fileDescriptor);
                            LOGE("Unable to write tif file");
                            throw_cant_open_file_exception(env, filePath);
                            return JNI_FALSE;
                        }
                    }
                }
            } else {
                if((output_image = TIFFOpen(strPath, "a")) == NULL){
                    LOGE("can not open file. Trying file descriptor");
                    //if TIFFOpen returns null then try to open file from descriptor
                    int mode = O_RDWR|O_CREAT;
                    fileDescriptor = open(strPath, mode, 0666);
                    if (fileDescriptor < 0) {
                        LOGE("Unable to create tif file descriptor");
                        throw_cant_open_file_exception(env, filePath);
                        return JNI_FALSE;
                    } else {
                        if ((output_image = TIFFFdOpen(fileDescriptor, strPath, "a")) == NULL) {
                            close(fileDescriptor);
                            LOGE("Unable to write tif file");
                            throw_cant_open_file_exception(env, filePath);
                            return JNI_FALSE;
                        }
                    }
                }
            }

            TIFFSetField(output_image, TIFFTAG_IMAGEWIDTH, img_width);
            TIFFSetField(output_image, TIFFTAG_IMAGELENGTH, img_height);
            TIFFSetField(output_image, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
            TIFFSetField(output_image, TIFFTAG_COMPRESSION, compressionInt);
            TIFFSetField(output_image, TIFFTAG_ORIENTATION, orientationInt);
            //TIFFSetField(output_image, TIFFTAG_XRESOLUTION, xRes);
            //TIFFSetField(output_image, TIFFTAG_YRESOLUTION, yRes);
            TIFFSetField(output_image, TIFFTAG_RESOLUTIONUNIT, RESUNIT_INCH);

            if (compressionInt == COMPRESSION_CCITTRLE ||compressionInt == COMPRESSION_CCITTFAX3 || compressionInt == COMPRESSION_CCITTFAX4) {
                TIFFSetField(output_image, TIFFTAG_BITSPERSAMPLE,	1);
                TIFFSetField(output_image, TIFFTAG_SAMPLESPERPIXEL,	1);
                TIFFSetField(output_image, TIFFTAG_ROWSPERSTRIP, 1);
                TIFFSetField(output_image, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_MINISBLACK);
                TIFFSetField(output_image, TIFFTAG_FILLORDER, FILLORDER_MSB2LSB);
            } else {
                TIFFSetField(output_image, TIFFTAG_BITSPERSAMPLE, 8);
                TIFFSetField(output_image, TIFFTAG_SAMPLESPERPIXEL, 4);
                TIFFSetField(output_image, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_RGB);
            }

            //Write additiona tags
            //CreationDate tag
            char *date = getCreationDate();
            TIFFSetField(output_image, TIFFTAG_DATETIME, date);
            free(date);
            //Host system
            TIFFSetField(output_image, TIFFTAG_HOSTCOMPUTER, fullReleaseName);


            //image description
            if (imgDescrString) {
                TIFFSetField(output_image, TIFFTAG_IMAGEDESCRIPTION, imgDescrString);
            }
            //author
            if (authorString) {
                TIFFSetField(output_image, TIFFTAG_ARTIST, authorString);
            }
            //copyright
            if (copyrightString) {
                TIFFSetField(output_image, TIFFTAG_COPYRIGHT, copyrightString);
            }

            // Write the information to the file
            if (compressionInt == COMPRESSION_CCITTRLE || compressionInt == COMPRESSION_CCITTFAX3 || compressionInt == COMPRESSION_CCITTFAX4) {
                unsigned char *bilevel = convertArgbToBilevel(img, img_width, img_height);
                int compressedWidth = ceil(img_width/8.0);
                for (int i = 0; i < img_height; i++) {
                    TIFFWriteEncodedStrip(output_image, i, &bilevel[i * compressedWidth], (compressedWidth));
                }
                free(bilevel);
            } else if (compressionInt == COMPRESSION_JPEG) {
                for (int row = 0; row < img_height; row++) {
                    TIFFWriteScanline(output_image, &img[row * img_width], row, 0);
                }
            } else {
                TIFFSetField(output_image, TIFFTAG_ROWSPERSTRIP, 1);
                for (int row = 0; row < img_height; row++) {
                    TIFFWriteEncodedStrip(output_image, row, &img[row * img_width], img_width * sizeof(uint32));
                    //TIFFWriteScanline(output_image, &img[row * img_width], row, 0);
                }
            }
           ret = TIFFWriteDirectory(output_image);
            LOGII("ret = ", ret);

            // Close the file
            TIFFClose(output_image);

            //if file descriptor was openned then close it

            if (fileDescriptor >= 0) {
                close(fileDescriptor);
            }
    /*
            //free temp array
            free (array);
    */
            if (tmpImgArrayCreated) {
                free(img);
            }

            //Now we don't need android pixels, so unlock
                     AndroidBitmap_unlockPixels(env, bitmap);

            //Remove variables
            if (releaseString) {
                env->ReleaseStringUTFChars(jrelease, releaseString);
            }
            free(fullReleaseName);

            if (imgDescrString) {
                env->ReleaseStringUTFChars(jImgDescr, imgDescrString);
            }
            if (authorString) {
                env->ReleaseStringUTFChars(jAuthor, authorString);
            }
            if (copyrightString) {
                env->ReleaseStringUTFChars(jCopyright, copyrightString);
            }
            env->ReleaseStringUTFChars(filePath, strPath);
    //        env->ReleaseIntArrayElements(img, c_array, 0);

            if (ret == -1) return JNI_FALSE;
            return JNI_TRUE;
}
unsigned char *convertArgbToBilevel(uint32 *source, jint width, jint height) {
        long long threshold = 0;
        uint32 crPix;
        uint32 grayPix;
        int bilevelWidth = width / 8.0;

        unsigned char *dest = (unsigned char *) malloc(sizeof(unsigned char) * bilevelWidth * height);

        uint32 maxGrey = (0.2125 * 255 + 0.7154 * 255 + 0.0721 * 255);
        uint32 halfGrey = maxGrey/2;

        uint32 shift = 0;
        unsigned char charsum = 0;
        int k = 7;
        for (int j = 0; j < height; j++) {
            shift = 0;
            charsum = 0;
            k = 7;
            for (int i = 0; i < width; i++) {
                crPix = source[j * width + i];
                grayPix = (0.2125 * (colorMask & crPix >> 16) + 0.7154 * (colorMask & crPix >> 8) + 0.0721 * (colorMask & crPix));

                if (grayPix < halfGrey) charsum &= ~(1 << k);
                else charsum |= 1 << k;

                if (k == 0) {
                    dest[j * bilevelWidth + shift] = charsum;
                    shift++;
                    k = 7;

                    charsum = 0;
                } else {
                    k--;
                }
            }
        }
        return dest;
    }

    char *getCreationDate() {
        char * datestr = (char *) malloc(sizeof(char) * 20);
        time_t rawtime;
        struct tm * timeinfo;
        time (&rawtime);
        timeinfo = localtime (&rawtime);
        strftime (datestr,20,/*"Now it's %I:%M%p."*/"%Y:%m:%d %H:%M:%S",timeinfo);

        return datestr;
    }

    char* concat(const char *s1, const char *s2)
    {
        char *result = (char *)malloc(strlen(s1)+strlen(s2)+1);//+1 for the zero-terminator
        //in real code you would check for errors in malloc here
        strcpy(result, s1);
        strcat(result, s2);
        return result;
    }
#ifdef __cplusplus
}
#endif
