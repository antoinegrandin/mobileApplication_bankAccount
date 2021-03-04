#include <jni.h>


JNIEXPORT jstring JNICALL
Java_com_antoinegrandin_bankaccount_AccountActivity_getKeys(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://60102f166c21e10017050128.mockapi.io/labbbank/");
}