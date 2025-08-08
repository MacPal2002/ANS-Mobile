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

# --- Twoje klasy danych i stanu (NAJWAŻNIEJSZE) ---
# Chroni modele danych (do Firestore) i klasy stanu (do UI) przed zmianą nazw,
# co zapobiega błędom przy serializacji i odczycie danych z bazy.
-keep class com.example.test1.data.** { *; }
-keep class com.example.test1.ui.login.LoginState { *; }
-keep class com.example.test1.ui.register.RegisterState { *; }
-keep class com.example.test1.ui.schedule.ScheduleState { *; }
-keep class com.example.test1.ui.settings.SettingsState { *; }


# --- Usuwanie logów z wersji produkcyjnej ---
# Ta reguła usuwa wszystkie wywołania Log.d, Log.v i Log.i z finalnego kodu,
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
