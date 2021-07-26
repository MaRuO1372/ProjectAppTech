-optimizationpasses 30
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-mergeinterfacesaggressively
-optimizations !code/simplification/arithmetic
-dontusemixedcaseclassnames
-allowaccessmodification
-useuniqueclassmembernames
-keeppackagenames doNotKeepAThing

-repackageclasses 'com'

-keep public class com.android.installreferrer.** { *; }
-keep class com.appsflyer.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp.internal.**

-keepattributes *Annotation*
-keepattributes Signature

-keep class com.google.** { *; }
-keep class com.foo.** { *; }
-keep class org.apache.** { *; }
-keep class com.android.** { *; }
-keep class junit.** { *; }
-keep class * implements android.os.Parcelable {*;}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class com.firebase. { *; }
-keep class org.shaded.apache. { *; }
-keepnames class com.shaded.fasterxml.jackson. { *; }
-keepnames class javax.servlet. { *; }
-keepnames class org.ietf.jgss. { *; }
-dontwarn org.w3c.dom.
-dontwarn org.joda.time.
-dontwarn org.shaded.apache.
-dontwarn org.ietf.jgss.**

-keep public class com.android.installreferrer.** { *; }