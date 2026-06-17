#include <jni.h>
#include <string>
#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <cerrno>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "NativeGadget"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

int native_enable_rndis(const char* udc_name) {
    std::string gadget_path = "";
    const char* bases[] = {"/config/usb_gadget", "/sys/kernel/config/usb_gadget", "/dev/usb-ffs"};
    
    for (int i = 0; i < 3; i++) {
        DIR* dir = opendir(bases[i]);
        if (dir) {
            struct dirent* entry;
            while ((entry = readdir(dir)) != nullptr) {
                if (strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0 && strstr(entry->d_name, "dummy") == nullptr) {
                    gadget_path = std::string(bases[i]) + "/" + entry->d_name;
                    break;
                }
            }
            closedir(dir);
            if (!gadget_path.empty()) break;
        }
    }
    
    if (gadget_path.empty()) {
        LOGE("No ConfigFS USB Gadget path found.");
        return -2;
    }
    
    std::string udc_path = gadget_path + "/UDC";
    
    int fd = open(udc_path.c_str(), O_WRONLY);
    if (fd < 0) {
        LOGE("Failed to open UDC file: %s (errno: %d)", udc_path.c_str(), errno);
        return -1;
    }
    
    if (udc_name != nullptr && strlen(udc_name) > 0) {
        ssize_t bytes_written = write(fd, udc_name, strlen(udc_name));
        if (bytes_written < 0) {
            LOGE("Failed to write to UDC file (errno: %d)", errno);
            close(fd);
            return -3;
        }
    } else {
        ssize_t bytes_written = write(fd, "\n", 1);
        if (bytes_written < 0) {
            LOGE("Failed to unbind UDC file (errno: %d)", errno);
            close(fd);
            return -3;
        }
    }
    
    close(fd);
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chimera_zpqmxr_utils_NativeBridge_enableRndisNative(JNIEnv* env, jobject thiz, jstring udcName) {
    const char* udc_name = nullptr;
    if (udcName != nullptr) {
        udc_name = env->GetStringUTFChars(udcName, nullptr);
    }
    
    int result = native_enable_rndis(udc_name);
    
    if (udc_name != nullptr) {
        env->ReleaseStringUTFChars(udcName, udc_name);
    }
    
    return result;
}
