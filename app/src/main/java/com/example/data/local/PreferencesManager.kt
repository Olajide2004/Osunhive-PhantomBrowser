package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "phantom_settings")

class PreferencesManager(private val context: Context) {
  companion object {
    val KEY_THEME = stringPreferencesKey("app_theme") // "DARK", "LIGHT", "SYSTEM"
    val KEY_NOTICE_ACCEPTED = booleanPreferencesKey("notice_accepted")
    val KEY_TRIAL_BANNER_DISMISSED = booleanPreferencesKey("trial_banner_dismissed")
    val KEY_LANGUAGE = stringPreferencesKey("app_language") // "English", etc.
  }

  val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[KEY_THEME] ?: "DARK"
  }

  val noticeAcceptedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_NOTICE_ACCEPTED] ?: false
  }

  val trialBannerDismissedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_TRIAL_BANNER_DISMISSED] ?: false
  }

  suspend fun setTheme(theme: String) {
    context.dataStore.edit { prefs ->
      prefs[KEY_THEME] = theme
    }
  }

  suspend fun setNoticeAccepted(accepted: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_NOTICE_ACCEPTED] = accepted
    }
  }

  suspend fun setTrialBannerDismissed(dismissed: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_TRIAL_BANNER_DISMISSED] = dismissed
    }
  }
}
