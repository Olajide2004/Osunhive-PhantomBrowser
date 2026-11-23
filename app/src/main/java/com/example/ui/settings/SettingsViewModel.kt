package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
  val theme: String = "DARK",
  val language: String = "English",
  val noticeAccepted: Boolean = true,
  val cacheBrowsingHistory: Boolean = true,
  val proxyAutoRotationInterval: String = "1 min",
  val exportedJson: String? = null,
  val statusMessage: String? = null,
  val totalProfiles: Int = 0
)

class SettingsViewModel(
  private val preferencesManager: PreferencesManager,
  private val profileRepository: ProfileRepository
) : ViewModel() {

  private val _statusMessage = MutableStateFlow<String?>(null)
  private val _exportedJson = MutableStateFlow<String?>(null)

  private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

  private val _baseSettingsFlow = combine(
    preferencesManager.themeFlow,
    preferencesManager.noticeAcceptedFlow,
    preferencesManager.cacheBrowsingHistoryFlow
  ) { theme, accepted, cache -> Triple(theme, accepted, cache) }

  private val _extraSettingsFlow = combine(
    preferencesManager.proxyAutoRotationIntervalFlow,
    profileRepository.allProfiles,
    _statusMessage,
    _exportedJson
  ) { autoRotate, profiles, status, json -> Quad(autoRotate, profiles, status, json) }

  val uiState: StateFlow<SettingsUiState> = combine(
    _baseSettingsFlow,
    _extraSettingsFlow
  ) { base, extra ->
    SettingsUiState(
      theme = base.first,
      noticeAccepted = base.second,
      cacheBrowsingHistory = base.third,
      proxyAutoRotationInterval = extra.first,
      exportedJson = extra.fourth,
      statusMessage = extra.third,
      totalProfiles = extra.second.size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SettingsUiState()
  )

  fun setTheme(theme: String) {
    viewModelScope.launch {
      preferencesManager.setTheme(theme)
    }
  }

  fun setCacheBrowsingHistory(cache: Boolean) {
    viewModelScope.launch {
      preferencesManager.setCacheBrowsingHistory(cache)
    }
  }

  fun setProxyAutoRotationInterval(interval: String) {
    viewModelScope.launch {
      preferencesManager.setProxyAutoRotationInterval(interval)
    }
  }

  fun exportProfiles() {
    viewModelScope.launch {
      profileRepository.allProfiles.collect { profiles ->
        val json = profileRepository.serializeProfilesToJson(profiles)
        _exportedJson.value = json
        _statusMessage.value = "Successfully exported ${profiles.size} profiles to JSON."
        return@collect
      }
    }
  }

  fun importProfiles(jsonString: String) {
    viewModelScope.launch {
      val result = profileRepository.importProfilesFromJson(jsonString)
      if (result.isSuccess) {
        val count = result.getOrNull() ?: 0
        _statusMessage.value = "Imported $count profiles successfully!"
      } else {
        _statusMessage.value = "Import failed: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun clearExportedJson() {
    _exportedJson.value = null
  }

  class Factory(
    private val preferencesManager: PreferencesManager,
    private val profileRepository: ProfileRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return SettingsViewModel(preferencesManager, profileRepository) as T
    }
  }
}
