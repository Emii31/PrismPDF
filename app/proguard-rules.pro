# Preserve JNI boundary layouts from compilation stripping
-keepclasseswithmembernames class * {
    native <methods>;
}

# Protect OpenCV programmatic linkage references from optimization elimination
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# Retain Android Graphics mapping configurations and type structures
-keep class android.graphics.** { *; }
