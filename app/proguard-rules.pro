# Strata — ProGuard / R8 Rules
# ============================================================
# These rules prevent R8 from stripping or renaming classes that
# are accessed via reflection, serialization, or JNI.
# ============================================================

# ---------- Debugging: preserve line numbers in stack traces --------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------- Kotlin ---------------------------------------------------
-keepclassmembers class **$WhenMappings { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { volatile <fields>; }
-dontwarn kotlin.**

# ---------- Kotlin Coroutines ----------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---------- Kotlin Serialization -------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable data classes and their fields
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**

# ---------- Ktor -----------------------------------------------------
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**

# ---------- OkHttp / Okio (used under the hood by Ktor) --------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ---------- Room Database --------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database class *
-dontwarn androidx.room.**

# ---------- Supabase -------------------------------------------------
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ---------- Coil (Image Loading) ------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# ---------- AndroidX Navigation ------------------------------------
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment {}

# ---------- Compose ------------------------------------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---------- WorkManager --------------------------------------------
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ---------- Crashlytics (optional — add if Firebase is added) ------
# -keepattributes *Annotation*
# -keep class com.google.firebase.** { *; }
# -keep class com.crashlytics.** { *; }

# ---------- App Models (prevent stripping of serialized fields) -----
-keep class com.example.strata.data.model.** { *; }
-keepclassmembers class com.example.strata.data.model.** { *; }