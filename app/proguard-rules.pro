-keepattributes Signature
-keepattributes *Annotation*

-adaptclassstrings
-adaptresourcefilecontents **/*.xml

-dontusemixedcaseclassnames
-useuniqueclassmembernames
-renamesourcefileattribute Null
-keepattributes SourceFile,LineNumberTable

-dontwarn rx.**
-dontwarn retrofit.Platform$*
-dontwarn retrofit.appengine.**
-dontwarn retrofit.converter.**
-dontwarn com.squareup.**
-dontwarn java.nio.**
-dontwarn org.w3c.dom.bootstrap.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn com.leanplum.**

-keep class com.staples.mobile.common.access.** { * ; }
-keep class com.staples.mobile.common.** { * ; }
-keep class com.staples.mobile.configurator.access.** { * ; }
-keep class com.staples.mobile.configurator.** { * ; }


-keep class com.squareup.retrofit.** { * ; }
-keep class com.fasterxml.jackson.** { * ; }
-keep class retrofit.http.** { * ; }
-keep class com.google.android.gms.** { * ; }

-assumenosideeffects class android.util.Log {
	public static int d(...);
	public static int e(...);
	public static int i(...);
    public static int println(...);
	public static int v(...);
	public static int w(...);
	public static int wtf(...);
}
