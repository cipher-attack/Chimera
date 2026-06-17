# Contributing to Chimera

If you're reading this, you want to get your hands dirty with the core engine. Welcome. 

Chimera is a low-level framework. Modifying the JNI bridges or ConfigFS routines can easily kernel-panic a test device if poorly handled. Please read these guidelines before opening a Pull Request.

## Development Setup

1. Clone the repository.
2. Ensure you have Android Studio installed with **NDK (v25+)** and **CMake**.
3. The project will fail to build if your `local.properties` is missing the correct `ndk.dir` path for the C++ compiler.

## Testing Constraints (No Emulators)

Do not test hardware emulation or gadget functions on Android Studio Emulators. Virtual environments lack `UDC` (USB Device Controller) and `ConfigFS` bindings.
*   **Requirement:** You must test your native builds on a physical, rooted device (Magisk/KernelSU) running a custom kernel with `CONFIG_USB_CONFIGFS_F_HID`.

## Working with Native Modules

If you modify `native_gadget.cpp` or the JNI hardware detection hooks:
*   **Memory Management:** Keep memory allocations tight. Memory leaks in the C++ layer will crash the Kotlin host app during heavy DuckyScript payload streaming.
*   **Error Handling:** Always wrap hardware sysfs reads in proper `try-catch` blocks on the Kotlin side to prevent unpredictable ANRs.

## Pull Requests

*   **Keep it Focused:** One PR per feature or bugfix. Don't rewrite the parser engine and add a UI toggle in the exact same PR.
*   **Pass the Build:** Ensure `./gradlew assembleDebug` compiles cleanly without JNI linking errors.
*   **Payloads:** If you are adding default payloads to the Hak5 repository integration, ensure they are heavily commented and harmless (strictly educational).

## Styleguide

We follow the standard Kotlin style guide. Refer to the provided `.editorconfig` before committing.
