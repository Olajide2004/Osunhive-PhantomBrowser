package com.example.ui.extendedsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.fingerprint.ConsistencyReport
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import com.example.data.repository.FolderRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.ProxyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ExtendedSection(val title: String, val icon: String, val subtitle: String) {
  OVERVIEW("Profile Overview", "badge", "Name, notes, OS, and active fingerprint status"),
  PROXY_LOCATION("Proxy & Location", "public", "Timezone, geolocation coordinates, and WebRTC leak protection"),
  BROWSER("Browser Engine", "language", "User-Agent string, start URL, launch arguments, and languages"),
  STORAGE("Storage & Sandboxing", "folder_special", "Per-profile isolation for cookies, cache, and history"),
  HARDWARE("Hardware Emulation", "memory", "Canvas noise, WebGL vendor, CPU cores, and device RAM")
}

data class ExtendedSettingsUiState(
  val profile: ProfileEntity? = null,
  val folders: List<FolderEntity> = emptyList(),
  val proxies: List<ProxyEntity> = emptyList(),
  val activeSection: ExtendedSection = ExtendedSection.OVERVIEW,
  val consistencyReport: ConsistencyReport? = null,
  val isSavedSuccessfully: Boolean = false,
  val errorMessage: String? = null
)

class ExtendedSettingsViewModel(
  private val profileId: String,
  private val profileRepository: ProfileRepository,
  private val folderRepository: FolderRepository,
  private val proxyRepository: ProxyRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ExtendedSettingsUiState())
  val uiState: StateFlow<ExtendedSettingsUiState> = _uiState.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      val loadedProfile = if (profileId == "new") {
        createDefaultNewProfile()
      } else {
        profileRepository.getProfileById(profileId) ?: createDefaultNewProfile()
      }

      folderRepository.allFolders.collect { folderList ->
        proxyRepository.allProxies.collect { proxyList ->
          _uiState.update { current ->
            current.copy(
              profile = loadedProfile,
              folders = folderList,
              proxies = proxyList,
              consistencyReport = calculateConsistency(loadedProfile)
            )
          }
        }
      }
    }
  }

  private fun createDefaultNewProfile(): ProfileEntity {
    val name = FingerprintGenerator.generateRandomName()
    val fp = FingerprintGenerator.generateConsistentFingerprint("Android", "US")
    return ProfileEntity(
      id = UUID.randomUUID().toString(),
      name = name,
      os = "Android",
      proxyCountry = "DIRECT",
      resolutionWidth = fp["resW"] as Int,
      resolutionHeight = fp["resH"] as Int,
      deviceMemoryGb = fp["memory"] as Int,
      cpuThreads = fp["threads"] as Int,
      webGlVendor = fp["gpuVendor"] as String,
      webGlRenderer = fp["gpuRenderer"] as String,
      timezone = fp["timezone"] as String,
      latitude = fp["latitude"] as Double,
      longitude = fp["longitude"] as Double,
      languages = fp["languages"] as String,
      userAgent = fp["userAgent"] as String,
      fingerprintSummary = "Freshly Generated Android Fingerprint"
    )
  }

  fun setSection(section: ExtendedSection) {
    _uiState.update { it.copy(activeSection = section) }
  }

  fun updateProfile(transform: (ProfileEntity) -> ProfileEntity) {
    _uiState.update { current ->
      val updated = current.profile?.let(transform) ?: return@update current
      current.copy(
        profile = updated,
        consistencyReport = calculateConsistency(updated),
        isSavedSuccessfully = false
      )
    }
  }

  fun generateNewFingerprint() {
    val current = _uiState.value.profile ?: return
    val proxy = _uiState.value.proxies.find { it.id == current.proxyId }
    val country = proxy?.countryCode ?: current.proxyCountry
    val fp = FingerprintGenerator.generateConsistentFingerprint(current.os, country)

    updateProfile { p ->
      p.copy(
        resolutionWidth = fp["resW"] as Int,
        resolutionHeight = fp["resH"] as Int,
        deviceMemoryGb = fp["memory"] as Int,
        cpuThreads = fp["threads"] as Int,
        webGlVendor = fp["gpuVendor"] as String,
        webGlRenderer = fp["gpuRenderer"] as String,
        timezone = fp["timezone"] as String,
        latitude = fp["latitude"] as Double,
        longitude = fp["longitude"] as Double,
        languages = fp["languages"] as String,
        userAgent = fp["userAgent"] as String,
        canvasNoiseEnabled = true,
        webGlSpoofing = true,
        audioNoiseEnabled = true,
        virtualCameraName = fp["vCamName"] as? String ?: p.virtualCameraName,
        fingerprintSummary = "Regenerated ${current.os} Fingerprint • Clean Entropy",
        updatedAt = System.currentTimeMillis()
      )
    }
  }

  private fun calculateConsistency(p: ProfileEntity): ConsistencyReport {
    return FingerprintGenerator.checkConsistency(
      os = p.os,
      userAgent = p.userAgent,
      proxyCountry = p.proxyCountry,
      timezone = p.timezone,
      width = p.resolutionWidth,
      height = p.resolutionHeight,
      gpuVendor = p.webGlVendor,
      gpuRenderer = p.webGlRenderer,
      webrtcPolicy = p.webrtcPolicy
    )
  }

  fun saveProfile(onSaved: () -> Unit) {
    val currentProfile = _uiState.value.profile ?: return
    viewModelScope.launch {
      val updated = currentProfile.copy(updatedAt = System.currentTimeMillis())
      profileRepository.insertProfile(updated)
      _uiState.update { it.copy(isSavedSuccessfully = true) }
      onSaved()
    }
  }

  class Factory(
    private val profileId: String,
    private val profileRepository: ProfileRepository,
    private val folderRepository: FolderRepository,
    private val proxyRepository: ProxyRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ExtendedSettingsViewModel(
        profileId,
        profileRepository,
        folderRepository,
        proxyRepository
      ) as T
    }
  }
}
