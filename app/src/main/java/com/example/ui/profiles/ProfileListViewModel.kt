package com.example.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.local.PreferencesManager
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import com.example.data.proxy.ProxyControllerManager
import com.example.data.repository.FolderRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.ProxyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ProfileListUiState(
  val profiles: List<ProfileEntity> = emptyList(),
  val folders: List<FolderEntity> = emptyList(),
  val proxies: List<ProxyEntity> = emptyList(),
  val selectedFolderId: String? = null,
  val searchQuery: String = "",
  val isTrialBannerDismissed: Boolean = false,
  val noticeAccepted: Boolean = true,
  val runningProfileId: String? = null,
  val totalProfilesCount: Int = 0
)

class ProfileListViewModel(
  private val profileRepository: ProfileRepository,
  private val folderRepository: FolderRepository,
  private val proxyRepository: ProxyRepository,
  private val preferencesManager: PreferencesManager
) : ViewModel() {

  private val _selectedFolderId = MutableStateFlow<String?>(null)
  private val _searchQuery = MutableStateFlow("")

  private val _filterCriteria = combine(_selectedFolderId, _searchQuery, ::Pair)
  private val _bannerAndNotice = combine(preferencesManager.trialBannerDismissedFlow, preferencesManager.noticeAcceptedFlow, ::Pair)

  val uiState: StateFlow<ProfileListUiState> = combine(
    profileRepository.allProfiles,
    folderRepository.allFolders,
    proxyRepository.allProxies,
    _filterCriteria,
    _bannerAndNotice
  ) { profiles, folders, proxies, (selectedFolder, search), (trialDismissed, noticeAccepted) ->
    val filtered = profiles.filter { profile ->
      val matchesFolder = selectedFolder == null || profile.folderId == selectedFolder
      val matchesSearch = search.isBlank() ||
        profile.name.contains(search, ignoreCase = true) ||
        profile.note.contains(search, ignoreCase = true) ||
        profile.proxyCountry.contains(search, ignoreCase = true)
      matchesFolder && matchesSearch
    }
    val running = profiles.find { it.isRunning }?.id

    ProfileListUiState(
      profiles = filtered,
      folders = folders,
      proxies = proxies,
      selectedFolderId = selectedFolder,
      searchQuery = search,
      isTrialBannerDismissed = trialDismissed,
      noticeAccepted = noticeAccepted,
      runningProfileId = running,
      totalProfilesCount = profiles.size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProfileListUiState()
  )

  fun selectFolder(folderId: String?) {
    _selectedFolderId.value = folderId
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun dismissTrialBanner() {
    viewModelScope.launch {
      preferencesManager.setTrialBannerDismissed(true)
    }
  }

  fun acceptNotice() {
    viewModelScope.launch {
      preferencesManager.setNoticeAccepted(true)
    }
  }

  fun launchProfile(profile: ProfileEntity, onLaunched: (ProfileEntity) -> Unit) {
    viewModelScope.launch {
      // First stop any currently running profile
      profileRepository.stopAllProfiles()
      profileRepository.setRunningState(profile.id, true)
      
      // Look up proxy if configured
      val proxy = profile.proxyId?.let { proxyRepository.getProxyById(it) }
      ProxyControllerManager.applyProxy(proxy) {
        onLaunched(profile.copy(isRunning = true))
      }
    }
  }

  fun stopProfile(profileId: String) {
    viewModelScope.launch {
      profileRepository.setRunningState(profileId, false)
      ProxyControllerManager.clearProxy()
    }
  }

  fun deleteProfile(profileId: String) {
    viewModelScope.launch {
      profileRepository.deleteProfile(profileId)
    }
  }

  fun duplicateProfile(profile: ProfileEntity) {
    viewModelScope.launch {
      val cloned = profile.copy(
        id = UUID.randomUUID().toString(),
        name = "${profile.name}-copy",
        isRunning = false,
        dataUsedMb = 0.0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      profileRepository.insertProfile(cloned)
    }
  }

  fun createFolder(name: String, colorHex: String) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val folder = FolderEntity(
        id = "folder_${UUID.randomUUID().toString().take(8)}",
        name = name.trim(),
        colorHex = colorHex
      )
      folderRepository.insertFolder(folder)
    }
  }

  fun quickCreateProfile(
    name: String,
    os: String,
    proxyId: String?,
    folderId: String?
  ) {
    viewModelScope.launch {
      val proxy = proxyId?.let { proxyRepository.getProxyById(it) }
      val proxyCountry = proxy?.countryCode ?: "DIRECT"
      val fp = FingerprintGenerator.generateConsistentFingerprint(os, proxyCountry)

      val profile = ProfileEntity(
        id = UUID.randomUUID().toString(),
        name = if (name.isNotBlank()) name else FingerprintGenerator.generateRandomName(),
        folderId = folderId,
        os = os,
        proxyId = proxyId,
        proxyCountry = proxyCountry,
        startUrl = "https://browserleaks.com/ip",
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
        fingerprintSummary = "Generated $os Stealth Fingerprint • $proxyCountry IP",
        updatedAt = System.currentTimeMillis()
      )
      profileRepository.insertProfile(profile)
    }
  }

  class Factory(
    private val profileRepository: ProfileRepository,
    private val folderRepository: FolderRepository,
    private val proxyRepository: ProxyRepository,
    private val preferencesManager: PreferencesManager
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ProfileListViewModel(
        profileRepository,
        folderRepository,
        proxyRepository,
        preferencesManager
      ) as T
    }
  }
}
