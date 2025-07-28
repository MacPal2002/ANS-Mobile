package com.example.test1.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Ten plik definiuje rozszerzenie DataStore dla całej aplikacji.
// Umieszczenie go w osobnym pliku jest dobrą praktyką i może pomóc
// rozwiązać problemy z kompilacją w niektórych środowiskach.
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
