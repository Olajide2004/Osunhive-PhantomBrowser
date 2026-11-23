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

  val uiState: StateFlow<SettingsUiState> = combine(
    preferencesManager.themeFlow,
    preferencesManager.noticeAcceptedFlow,
    profileRepository.allProfiles,
    _statusMessage,
    _exportedJson
  ) { theme, accepted, profiles, status, json ->
    SettingsUiState(
      theme = theme,
      noticeAccepted = accepted,
      exportedJson = json,
      statusMessage = status,
      totalProfiles = profiles.size
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
