#include <jni.h>
#include <string>
#include <fcntl.h>
#include <unistd.h>
#include <cerrno>
#include <cstring>
#include <cstdlib>
#include <sys/wait.h>
#include <android/log.h>

#define LOG_TAG "NativeIO"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_chimera_zpqmxr_utils_NativeIOManager_checkRootNative(JNIEnv* env, jobject thiz) {
    int ret = system("su -c id > /dev/null 2>&1");
    
    
    
    if (ret != -1 && WEXITSTATUS(ret) == 0) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chimera_zpqmxr_utils_NativeIOManager_writeBytesNative(JNIEnv* env, jobject thiz, jstring path, jbyteArray data) {
    if (path == nullptr || data == nullptr) {
        return -1;
    }
    const char* file_path = env->GetStringUTFChars(path, nullptr);
    if (!file_path) return -1;
    
    int fd = open(file_path, O_WRONLY | O_APPEND);
    if (fd < 0) {
        fd = open(file_path, O_WRONLY);
        if (fd < 0) {
            LOGE("Failed to open %s for writing: %d", file_path, errno);
            env->ReleaseStringUTFChars(path, file_path);
            return -1;
        }
    }
    
    jsize len = env->GetArrayLength(data);
    jbyte* bytes = env->GetByteArrayElements(data, nullptr);
    if (!bytes) {
        close(fd);
        env->ReleaseStringUTFChars(path, file_path);
        return -1;
    }
    
    ssize_t written = write(fd, bytes, len);
    if (written < 0) {
        LOGE("Failed to write bytes to %s: %d", file_path, errno);
    }
    
    close(fd);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(path, file_path);
    
    return (written >= 0) ? 0 : -1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chimera_zpqmxr_utils_NativeIOManager_writeStringNative(JNIEnv* env, jobject thiz, jstring path, jstring value) {
    if (path == nullptr || value == nullptr) {
        return -1;
    }
    const char* file_path = env->GetStringUTFChars(path, nullptr);
    if (!file_path) return -1;
    
    int fd = open(file_path, O_WRONLY);
    if (fd < 0) {
        LOGE("Failed to open %s for string writing: %d", file_path, errno);
        env->ReleaseStringUTFChars(path, file_path);
        return -1;
    }
    
    const char* str_val = env->GetStringUTFChars(value, nullptr);
    if (!str_val) {
        close(fd);
        env->ReleaseStringUTFChars(path, file_path);
        return -1;
    }
    
    ssize_t len = strlen(str_val);
    ssize_t written = write(fd, str_val, len);
    if (written < 0) {
        LOGE("Failed to write string to %s: %d", file_path, errno);
    }
    
    close(fd);
    env->ReleaseStringUTFChars(value, str_val);
    env->ReleaseStringUTFChars(path, file_path);
    
    return (written >= 0) ? 0 : -1;
}
