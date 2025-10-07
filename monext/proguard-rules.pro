# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================
# MONEXT SDK - ProGuard Rules
# ============================================

# Fonctions Kotlin de niveau package (fichiers .kt)
-keep public class com.monext.sdk.PaymentBoxKt {
    public *;
}
-keep public class com.monext.sdk.PaymentSheetKt {
    public *;
}
-keep public class com.monext.sdk.MnxtSDKContextKt {
    public *;
}

# Classes de configuration
-keep public class com.monext.sdk.Appearance {
    *;
}
-keep public enum com.monext.sdk.Appearance$PaymentMethodShape {
    *;
}
# Toutes les autres classes imbriquées (Defaults, Colors, etc.)
-keep class com.monext.sdk.Appearance$** {
    *;
}
-keep public class com.monext.sdk.GooglePayConfiguration {
    public *;
}
-keep public class com.monext.sdk.MnxtSDKConfiguration {
    public *;
}
-keep public class com.monext.sdk.MnxtSDKContext {
    public *;
}

# Sealed classes - Garder la classe parent ET tous les héritiers
-keep public class com.monext.sdk.MnxtEnvironment {
    public *;
}
-keep public class com.monext.sdk.MnxtEnvironment$Sandbox {
    *;
}
-keep public class com.monext.sdk.MnxtEnvironment$Production {
    *;
}
-keep public class com.monext.sdk.MnxtEnvironment$Custom {
    *;
}

-keep public class com.monext.sdk.PaymentResult {
    public *;
}
-keep public class com.monext.sdk.PaymentResult$* {
    *;
}

# Companion objects des classes publiques
-keep class com.monext.sdk.Appearance$Companion { *; }
-keep class com.monext.sdk.GooglePayConfiguration$Companion { *; }
-keep class com.monext.sdk.MnxtSDKConfiguration$Companion { *; }
-keep class com.monext.sdk.MnxtSDKContext$Companion { *; }
-keep class com.monext.sdk.MnxtEnvironment$Companion { *; }
-keep class com.monext.sdk.PaymentResult$Companion { *; }

# Enums publics
-keepclassmembers public enum com.monext.sdk.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# ============================================
# PACKAGE INTERNAL - OBFUSQUER
# ============================================
-keep,allowobfuscation,allowshrinking class com.monext.sdk.internal.** { *; }

# ============================================
# KOTLIN ESSENTIALS
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Metadata Kotlin
-keep class kotlin.Metadata { *; }

# WhenMappings (générés par le compilateur)
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Data classes
-keepclassmembers class com.monext.sdk.** {
    public ** component*();
    public ** copy(...);
}

# ============================================
# KOTLINX SERIALIZATION
# ============================================
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# ============================================
# JETPACK COMPOSE
# ============================================
-keep class androidx.compose.runtime.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ============================================
# COROUTINES
# ============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================
# GOOGLE PAY
# ============================================
-keep class com.google.android.gms.wallet.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Google Pay Button (bibliothèque pay-button)
-keep class com.google.pay.button.** { *; }
-keep enum com.google.pay.button.ButtonTheme { *; }
-keep enum com.google.pay.button.ButtonType { *; }
-keep public class com.google.pay.button.** { *; }
-keep public enum com.google.pay.button.ButtonTheme { *; }
-keep public enum com.google.pay.button.ButtonType { *; }


# ============================================
# NETCETERA 3DS SDK
# ============================================
-keeppackagenames com.netcetera.threeds.sdk.api.**

-keep public class com.netcetera.threeds.sdk.** {
    public protected <fields>;
    public protected <methods>;
}

-keep,allowshrinking class com.netcetera.threeds.sdk.** {
    <fields>;
    <methods>;
}

-keep class org.bouncycastle.** {
    <fields>;
    <methods>;
}

-keep,allowshrinking class org.bouncycastle.** {
    <fields>;
    <methods>;
}

-keep class org.slf4j.** {
    <fields>;
    <methods>;
}

-keep class kotlin.KotlinVersion {
    <fields>;
    <methods>;
}

# ============================================
# PARCELIZE
# ============================================
-keep @kotlinx.parcelize.Parcelize class * { *; }

# ============================================
# DEBUGGING
# ============================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# OPTIMISATIONS
# ============================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# ============================================
# SUPPRESSION DES LOGS
# ============================================
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class org.slf4j.Logger {
    public void trace(...);
    public void debug(...);
    public void info(...);
}