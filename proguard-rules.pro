# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/mattsilber/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-printmapping mapping.txt
-verbose
-dontoptimize
-dontshrink
-dontskipnonpubliclibraryclassmembers
-dontusemixedcaseclassnames

-keep class android.support.v8.renderscript.** { *; }
-keepclassmembers class android.support.v8.renderscript.** { *; }

-keep class com.caverock.androidsvg.** { *; }
-keepclassmembers class com.caverock.androidsvg.** { *; }

-keep class com.guardanis.imageloader.** { *; }
-keepclassmembers class com.guardanis.imageloader.** { *; }