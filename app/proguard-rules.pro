# Ten plik zawiera reguły dla R8, które chronią kod przed usunięciem (shrinking)
# lub zmianą nazw (obfuscation) podczas budowania wersji produkcyjnej (release).

# --- Reguły ogólne ---
# Zachowuje informacje potrzebne do debugowania crashy (stack trace).
-keepattributes Signature, InnerClasses, EnclosingMethod

# --- Firebase ---
# Oficjalne reguły zalecane przez Firebase. Zapobiegają błędom związanym
# z refleksją i wymaganymi nazwami klas.
-keep class com.google.firebase.provider.FirebaseInitProvider
-keep class com.google.android.gms.common.api.internal.TaskApiCall
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.installations.** { *; }
-keep class com.google.android.gms.common.** { *; }


# --- Kotlin Coroutines ---
# Zachowuje nazwy klas kluczowych dla poprawnego działania korutyn.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory
-keepnames class kotlinx.coroutines.DefaultExecutor
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# --- Jetpack Compose ---
# Zapewnia poprawne działanie środowiska wykonawczego Compose.
-keepclassmembers class * implements androidx.compose.runtime.Composer {
  <init>(...);
}

# --- Hilt (Wstrzykiwanie Zależności) ---
# Chroni klasy generowane przez Hilt.
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedEntryPoint { *; }
-keep class dagger.hilt.internal.processedrootsentinel.codegen.*
-keep class dagger.hilt.android.internal.managers.*
-keep class dagger.hilt.android.internal.modules.ApplicationContextModule

# --- Gson / Room Type Converters ---
# BARDZO WAŻNE: Zapobiega awarii aplikacji przy deserializacji obiektów z JSON,
# zwłaszcza przy użyciu TypeToken w TypeConverterach.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }


# ---  Klasy danych i stanu ---
# Chroni modele danych (do Firestore) i klasy stanu (do UI) przed zmianą nazw,
# co zapobiega błędom przy serializacji i odczycie danych z bazy.
# Ta reguła jest bezpieczna i obejmuje wszystkie klasy w pakiecie 'data'.
-keep class com.example.test1.data.** { *; }
# używamy wzorca, który dopasuje wszystkie klasy kończące się na "State".
-keep class com.example.test1.ui.**.*State { *; }


# --- Usuwanie logów z wersji produkcyjnej ---
# Ta reguła usuwa wszystkie wywołania Log.d, Log.v i Log.i z finalnego kodu,
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
