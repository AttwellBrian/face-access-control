##Setup on Mac OS X
Setup the Android SDK and NDK. I placed the NDK at `/Applications/android-ndk-r8d/ndk-build `.

Switch your Eclipse workspace to the root directory. Everything should be setup automatically since the directory contains a `.metadata` file.

##samples/face-detection

This contains the application I'm building on top of. The loads a native and mostly-native java-based face detector. In the future, we can probably remove the native detector.
