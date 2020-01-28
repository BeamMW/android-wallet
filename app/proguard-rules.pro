-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclassmembers
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify

##########
# Android
##########
-keep class * extends android.support.v7.app.AppCompatActivity
-keep public class * extends java.lang.Exception
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keep class * extends com.mw.beam.beamwallet.base_screen.BaseActivity
-keep class * extends com.mw.beam.beamwallet.base_screen.BaseFragment
-keep class com.mw.beam.beamwallet.core.Api
-keep class com.mw.beam.beamwallet.core.AppConfig
-keep,includedescriptorclasses class com.mw.beam.beamwallet.core.views.PasswordStrengthView { *; }
-keep,includedescriptorclasses class com.mw.beam.beamwallet.core.entities.** { *; }
-keep,includedescriptorclasses class com.mw.beam.beamwallet.core.listeners.** { *; }
-keep,includedescriptorclasses class com.mw.beam.beamwallet.core.helpers.** { *; }
-keep,includedescriptorclasses class com.mw.beam.beamwallet.screens.settings.SettingsFragmentMode { *; }
-keep,includedescriptorclasses class com.mw.beam.beamwallet.screens.settings.** { *; }

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

# View - Getters and setters - keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# For native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

#Enums - For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * { *; }

# Parcelables: Mantain the parcelables working
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Kotlin
-dontwarn kotlin.**
-dontnote kotlin.**
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.jetbrains.annotations.**
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.jvm.internal.Reflection
-dontnote kotlin.jvm.functions.Function1
-dontwarn javax.annotation.**
-keep class kotlin.reflect.jvm.internal.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

########################################
# External Libraries
########################################

# Google Play Services
-keepattributes *Annotation*
-keep class com.google.android.gms.* {  *; }
-dontwarn com.google.android.gms.**
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-dontnote **ILicensingService
-dontnote com.google.android.gms.gcm.GcmListenerService
-dontnote com.google.android.gms.**
-keep,includedescriptorclasses class io.fabric.sdk.android.** { *; }

# Android Support Lib
-keep class android.support.design.widget.TextInputLayout { *; }

#crashlytics
-keep,includedescriptorclasses class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-dontwarn com.google.firebase.crash.**

#barcode
-keep,includedescriptorclasses class com.google.zxing.qrcode.decoder.** { *; }
-keep,includedescriptorclasses class com.journeyapps.barcodescanner.** { *; }

#rxjava
-keep,includedescriptorclasses class io.reactivex.internal.** { *; }
-keep,includedescriptorclasses class io.reactivex.observers.** { *; }

-keep,includedescriptorclasses class android.arch.lifecycle.** { *; }
-keep,includedescriptorclasses class com.squareup.leakcanary.** { *; }


