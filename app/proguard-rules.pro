# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\gorgue\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

#General options
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes JavascriptInterface
-keepattributes **

-dontwarn com.squareup.okhttp.*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-dontwarn org.springframework.**
-dontwarn org.codehaus.jackson.**
-dontwarn com.fasterxml.**
-dontwarn com.google.**
-keep class org.springframework.** {*; }
-keep class org.codehaus.jackson.** {*; }
-keep class com.fasterxml.** {*; }
-keep class fasterxml.** { *; }

-keep class org.xwalk.core.** {*;}
-keep class org.chromium.** {*;}
-keep class cn.pedant.** {*;}
-keep class org.apache.** {*;}
-keep class java.io.** {*;}

-keep public class com.mobile.android.smartick.activities.MainActivity$JsInterface.** {*;}
-keep public class com.mobile.android.smartick.activities.MainActivity$JsScrollInterface.** {*;}
-keep public class com.mobile.android.smartick.activities.MainActivity$JsYouTubeInterface.** {*;}
-keep public class com.mobile.android.smartick.activities.MainActivity$MyRedirectHandler.** {*;}

-keepclassmembers class * {
    @com.mobile.android.smartick.activities.MainActivity.JsInterface <methods>;
    @com.mobile.android.smartick.activities.MainActivity.JsScrollInterface <methods>;
    @com.mobile.android.smartick.activities.MainActivity.JsYouTubeInterface <methods>;
    @com.mobile.android.smartick.activities.MainActivity.MyRedirectHandler <methods>;
}

#Optimization
-optimizationpasses 3
-allowaccessmodification
-assumenosideeffects class android.util.Log {
public static *** d(...);
public static *** w(...);
public static *** v(...);
public static *** i(...);
}

#Obfuscation
-adaptresourcefilenames **.png
-repackageclasses ''
-useuniqueclassmembernames
