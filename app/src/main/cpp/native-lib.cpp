#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_chan_mars_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_chan_mars_MainActivity_read(JNIEnv *env, jobject instance, jbyteArray data_, jint len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO

    env->ReleaseByteArrayElements(data_, data, 0);
}