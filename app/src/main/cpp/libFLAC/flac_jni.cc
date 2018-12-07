//
// Created by wangrl on 18-12-6.
//
#include <jni.h>
#include <android/log.h>
#include <cstdlib>
#include "include/flac_parser.h"

#define LOG_TAG "flac_jni"

#define DECODER_FUNC()

class JavaDataSource : public DataSource {
public:
    void setFlacDecoderJni(JNIEnv *env, jobject flacDecoderJni) {
        this->env = env;
        this->flacDecoderJni = flacDecoderJni;
        if (mid == NULL) {
            jclass cls = env->GetObjectClass(flacDecoderJni);
            mid = env->GetMethodID(cls, "read", "(Ljava/nio/ByteBuffer;)I");
            env->DeleteLocalRef(cls);
        }
    }

    ssize_t readAt(off64_t offset, void *const data, size_t size) {
        jobject byteBuffer = env->NewDirectByteBuffer(data, size);
        int result = env->CallIntMethod(flacDecoderJni, mid, byteBuffer);
        if (env->ExceptionCheck()) {
            // Exception is thrown in Java when returning from the native call.
            result = -1;
        }
        env->DeleteLocalRef(byteBuffer);
        return result;
    }

private:
    JNIEnv *env;
    jobject flacDecoderJni;
    jmethodID mid;
};

struct Context {
    JavaDataSource *source;
    FLACParser *parser;

    Context() {
        source = new JavaDataSource();
        parser = new FLACParser();
    }

    ~Context() {
        delete parser;
        delete source;
    }
};

JNIEXPORT jlong JNICALL
Java_com_android_approjects_exoplayer_extension_flac_FlacDecoderJni_flacInit(JNIEnv *env,
                                                                             jobject instance) {

    // TODO
    Context *context = new Context;
    if (!context->parser->init()) {
        delete context;
        return 0;
    }
    return reinterpret_cast<intptr_t >(context);
}



