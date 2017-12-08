# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# 保持指定源代码
-keep class cn.com.pyc.xcoder.XCoder{*;}
-keep class cn.com.pyc.xcoder.XCoder$SmFileStruct{*;}
-keep class com.artifex.mupdfdemo.*{*;}
-keep class tv.danmaku.ijk.media.player.*{*;}
-keep class cn.com.pyc.bean.event.*{*;}

-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}


# pbbonline混淆设置

-dontwarn javax.naming.**
-dontwarn junit.textui.**
-dontwarn java.awt.**
-dontwarn org.springframework.http.**
-dontwarn android.support.v4.**
-dontwarn jcifs.**

# 引用jar包
#-libraryjars /libs/android-support-v4.jar
#-libraryjars /libs/eventbus-2.4.0.jar
#-libraryjars /libs/fastjson-1.1.26.jar
#-libraryjars /libs/jpush-android-2.0.5.jar
#-libraryjars /libs/android-core-3.1.1-SNAPSHOT.jar
#-libraryjars /libs/core-3.1.1-SNAPSHOT.jar


# 第三方jar包中的类
-keep class android.support.** { *; }
-keep class de.greenrobot.event.** {*;}
-keep class com.alibaba.fastjson.** {*;}
-keep class org.apache.** {*;}
-keep class org.bouncycastle.** {*;}
-keep class org.json.** {*;}
-keep class com.google.zxing.** {*;}
-keep class net.** {*;}


-keep public class android.webkit.**
-keep public class * implements java.io.Serializable{
  public protected private *;
}

# 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 使用反射类
-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * extends java.lang.annotation.reflect { *; }
-keep class * extends java.lang.reflect { *; }

# 指定内容不混淆
-keep public class * extends net.sqlcipher.**
-keep class cn.com.pyc.pbbonline.bean.** {*;}
-keep class cn.com.pyc.pbbonline.model.** {*;}
-keep class com.sz.mobilesdk.authentication.** {*;}
-keep class com.sz.mobilesdk.database.** {*;}
-keep class com.sz.mobilesdk.models.** {*;}
-keep class cn.com.pyc.pbbonline.db.Shared {*;}
-keep class cn.com.pyc.loger.** {*;}
-keep class cn.com.pyc.model.** {*;}


# eventbus不混淆onEvent**开头的method
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keepclassmembers class ** {
    public void *Click(**);
    public void *Click1(**);
}

-keepclasseswithmembers class * {
    *** *Callback(...);
}

######## xUtils3
-keep class org.xutils.** { *; }
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}

######## JPush
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
# 2.0.5 JPush增加混淆
#==================gson==========================
-dontwarn com.google.**
-keep class com.google.gson.** {*;}

#==================protobuf======================
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

