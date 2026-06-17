#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <android/log.h>
#include <map>
#include <algorithm>
#include <regex>

#define LOG_TAG "HardwareDetector"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace NetHunter {
    struct HWDevice {
        std::string type;
        std::string name;
        std::string vendorId;
        std::string productId;
        std::string driver;
        std::string path;
        std::map<std::string, std::string> attributes;
    };

    class HardwareDetector {
    private:
        std::string readFile(const std::string& path) {
            std::ifstream file(path);
            if (!file.is_open()) return "";
            std::stringstream buffer;
            buffer << file.rdbuf();
            std::string content = buffer.str();
            
            if (!content.empty() && content.back() == '\n') {
                content.pop_back();
            }
            return content;
        }

        bool directoryExists(const std::string& path) {
            struct stat info;
            if (stat(path.c_str(), &info) != 0) {
                return false;
            }
            return (info.st_mode & S_IFDIR) != 0;
        }

        std::vector<std::string> listDirectories(const std::string& path) {
            std::vector<std::string> dirs;
            DIR* dir = opendir(path.c_str());
            if (dir) {
                struct dirent* entry;
                while ((entry = readdir(dir)) != nullptr) {
                    if (entry->d_type == DT_DIR || entry->d_type == DT_LNK) {
                        std::string name = entry->d_name;
                        if (name != "." && name != "..") {
                            dirs.push_back(name);
                        }
                    }
                }
                closedir(dir);
            }
            return dirs;
        }

    public:
        HardwareDetector() {}

        std::map<std::string, std::string> getCPUInfo() {
            std::map<std::string, std::string> cpuInfo;
            std::ifstream file("/proc/cpuinfo");
            std::string line;
            while (std::getline(file, line)) {
                auto delimiterPos = line.find(':');
                if (delimiterPos != std::string::npos) {
                    std::string key = line.substr(0, delimiterPos);
                    std::string value = line.substr(delimiterPos + 1);
                    
                    key.erase(0, key.find_first_not_of(" \t"));
                    key.erase(key.find_last_not_of(" \t") + 1);
                    value.erase(0, value.find_first_not_of(" \t"));
                    if (!key.empty() && cpuInfo.find(key) == cpuInfo.end()) {
                        cpuInfo[key] = value;
                    }
                }
            }
            return cpuInfo;
        }

        std::map<std::string, std::string> getMemInfo() {
            std::map<std::string, std::string> memInfo;
            std::ifstream file("/proc/meminfo");
            std::string line;
            while (std::getline(file, line)) {
                auto delimiterPos = line.find(':');
                if (delimiterPos != std::string::npos) {
                    std::string key = line.substr(0, delimiterPos);
                    std::string value = line.substr(delimiterPos + 1);
                    key.erase(0, key.find_first_not_of(" \t"));
                    key.erase(key.find_last_not_of(" \t") + 1);
                    value.erase(0, value.find_first_not_of(" \t"));
                    memInfo[key] = value;
                }
            }
            return memInfo;
        }

        std::vector<HWDevice> detectUSBDevices() {
            std::vector<HWDevice> devices;
            std::string basePath = "/sys/bus/usb/devices/";
            auto dirs = listDirectories(basePath);

            for (const auto& d : dirs) {
                if (d.find(':') != std::string::npos) continue;
                if (d == "usb1" || d == "usb2") continue;

                HWDevice dev;
                dev.type = "USB";
                dev.path = basePath + d;
                dev.vendorId = readFile(dev.path + "/idVendor");
                dev.productId = readFile(dev.path + "/idProduct");
                dev.name = readFile(dev.path + "/manufacturer") + " " + readFile(dev.path + "/product");
                
                if (dev.vendorId == "0cf3" && dev.productId == "9271") {
                    dev.attributes["Capabilities"] = "Monitor Mode, Packet Injection (Atheros AR9271)";
                } else if (dev.vendorId == "148f" && dev.productId == "5370") {
                    dev.attributes["Capabilities"] = "Monitor Mode, Packet Injection (Ralink RT5370)";
                } else if (dev.vendorId == "0bda" && dev.productId == "8187") {
                    dev.attributes["Capabilities"] = "Monitor Mode, Packet Injection (Realtek RTL8187)";
                }

                if (!dev.vendorId.empty()) {
                    devices.push_back(dev);
                }
            }
            return devices;
        }

        std::vector<HWDevice> detectNetworkInterfaces() {
            std::vector<HWDevice> interfaces;
            std::string basePath = "/sys/class/net/";
            auto dirs = listDirectories(basePath);

            for (const auto& ifaceName : dirs) {
                HWDevice dev;
                dev.type = "Network";
                dev.name = ifaceName;
                dev.path = basePath + ifaceName;
                dev.attributes["mac"] = readFile(dev.path + "/address");
                dev.attributes["operstate"] = readFile(dev.path + "/operstate");
                
                if (directoryExists(dev.path + "/wireless") || directoryExists(dev.path + "/phy80211")) {
                    dev.attributes["wireless"] = "true";
                }
                
                interfaces.push_back(dev);
            }
            return interfaces;
        }

        std::vector<std::string> detectUDCs() {
            std::vector<std::string> udcs;
            std::string basePath = "/sys/class/udc/";
            auto dirs = listDirectories(basePath);
            for (const auto& d : dirs) {
                udcs.push_back(d);
            }
            return udcs;
        }

        std::vector<HWDevice> detectBlockDevices() {
            std::vector<HWDevice> blocks;
            std::string basePath = "/sys/block/";
            auto dirs = listDirectories(basePath);
            for (const auto& blkName : dirs) {
                if (blkName.find("loop") == 0 || blkName.find("ram") == 0) continue;
                HWDevice dev;
                dev.type = "BlockStorage";
                dev.name = blkName;
                dev.path = basePath + blkName;
                dev.attributes["size"] = readFile(dev.path + "/size");
                dev.attributes["ro"] = readFile(dev.path + "/ro");
                
                if (blkName.find("mmcblk") == 0) {
                    dev.attributes["media_type"] = "SD/eMMC";
                }
                
                blocks.push_back(dev);
            }
            return blocks;
        }

        std::vector<HWDevice> detectI2cDevices() {
            std::vector<HWDevice> i2c;
            std::string basePath = "/sys/bus/i2c/devices/";
            auto dirs = listDirectories(basePath);
            for (const auto& d : dirs) {
                HWDevice dev;
                dev.type = "I2C";
                dev.name = d;
                dev.path = basePath + d;
                dev.attributes["name"] = readFile(dev.path + "/name");
                i2c.push_back(dev);
            }
            return i2c;
        }

        std::string performDeepScan() {
            std::stringstream report;
            report << "{\n";
            
            report << "  \"cpu\": {\n";
            auto cpu = getCPUInfo();
            report << "    \"model\": \"" << cpu["Hardware"] << "\",\n";
            report << "    \"architecture\": \"" << cpu["CPU architecture"] << "\"\n";
            report << "  },\n";

            auto udcs = detectUDCs();
            report << "  \"udc\": [\n";
            for (size_t i = 0; i < udcs.size(); ++i) {
                report << "    \"" << udcs[i] << "\"" << (i < udcs.size() - 1 ? "," : "") << "\n";
            }
            report << "  ],\n";

            auto net = detectNetworkInterfaces();
            report << "  \"network\": [\n";
            for (size_t i = 0; i < net.size(); ++i) {
                report << "    {\n";
                report << "      \"interface\": \"" << net[i].name << "\",\n";
                report << "      \"mac\": \"" << net[i].attributes["mac"] << "\",\n";
                if (net[i].attributes.count("wireless")) {
                    report << "      \"type\": \"wireless\"\n";
                } else {
                    report << "      \"type\": \"wired\"\n";
                }
                report << "    }" << (i < net.size() - 1 ? "," : "") << "\n";
            }
            report << "  ],\n";

            auto usb = detectUSBDevices();
            report << "  \"usb_devices\": [\n";
            for (size_t i = 0; i < usb.size(); ++i) {
                report << "    {\n";
                report << "      \"name\": \"" << usb[i].name << "\",\n";
                report << "      \"vid_pid\": \"" << usb[i].vendorId << ":" << usb[i].productId << "\",\n";
                if (usb[i].attributes.count("Capabilities")) {
                    report << "      \"nethunter_capabilities\": \"" << usb[i].attributes["Capabilities"] << "\"\n";
                }
                report << "    }" << (i < usb.size() - 1 ? "," : "") << "\n";
            }
            auto blocks = detectBlockDevices();
            report << "  \"block_storage\": [\n";
            for (size_t i = 0; i < blocks.size(); ++i) {
                report << "    {\n";
                report << "      \"device\": \"" << blocks[i].name << "\",\n";
                report << "      \"size_blocks\": \"" << blocks[i].attributes["size"] << "\",\n";
                report << "      \"read_only\": \"" << blocks[i].attributes["ro"] << "\"\n";
                report << "    }" << (i < blocks.size() - 1 ? "," : "") << "\n";
            }
            report << "  ],\n";

            auto i2cDevices = detectI2cDevices();
            report << "  \"i2c_devices\": [\n";
            for (size_t i = 0; i < i2cDevices.size(); ++i) {
                report << "    {\n";
                report << "      \"address_or_id\": \"" << i2cDevices[i].name << "\",\n";
                report << "      \"name\": \"" << i2cDevices[i].attributes["name"] << "\"\n";
                report << "    }" << (i < i2cDevices.size() - 1 ? "," : "") << "\n";
            }
            report << "  ]\n";
            
            report << "}\n";
            return report.str();
        }
    };
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_chimera_zpqmxr_utils_NativeBridge_performDeepHardwareScan(JNIEnv *env, jobject thiz) {
    NetHunter::HardwareDetector detector;
    std::string report = detector.performDeepScan();
    return env->NewStringUTF(report.c_str());
}
