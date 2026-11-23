package com.example.ui.profiles.create

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
import com.example.data.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ProxyMode {
  DIRECT,
  SAVED,
  CUSTOM
}

enum class PrivacyPreset {
  MAXIMUM_STEALTH,
  BALANCED,
  FAST,
  CUSTOM
}

data class CreateProfileUiState(
  // Basic Information
  val name: String = "",
  val note: String = "",
  val os: String = "Android",
  val folderId: String? = null,
  val folders: List<FolderEntity> = emptyList(),
  val proxies: List<ProxyEntity> = emptyList(),

  // Proxy Settings
  val proxyMode: ProxyMode = ProxyMode.DIRECT,
  val selectedProxyId: String? = null,
  val customProxyProtocol: String = "HTTP",
  val customProxyHost: String = "",
  val customProxyPort: String = "8080",
  val customProxyUsername: String = "",
  val customProxyPassword: String = "",
  val customProxyCountry: String = "US",
  val saveCustomProxyToPool: Boolean = false,
  val autoMatchLocationToProxy: Boolean = true,
  val timezone: String = "Based on IP",
  val latitude: Double = 37.7749,
  val longitude: Double = -122.4194,

  // Privacy & Fingerprint Settings
  val privacyPreset: PrivacyPreset = PrivacyPreset.MAXIMUM_STEALTH,
  val canvasNoiseEnabled: Boolean = true,
  val webGlSpoofing: Boolean = true,
  val webGlVendor: String = "Qualcomm",
  val webGlRenderer: String = "Adreno (TM) 750",
  val audioNoiseEnabled: Boolean = true,
  val virtualCameraEnabled: Boolean = true,
  val virtualCameraName: String = "Logitech HD Pro Webcam C920",
  val virtualCameraMode: String = "Color Bars & Timer",
  val virtualCameraFps: Int = 30,
  val virtualCameraMediaType: String = "SYNTHETIC", // "SYNTHETIC", "IMAGE", "VIDEO"
  val virtualCameraMediaUri: String? = null,
  val virtualCameraMediaBase64: String? = null,
  val virtualCameraThumbnail: android.graphics.Bitmap? = null,
  val webrtcPolicy: String = "Proxy IP Only",
  val dnsPolicy: String = "Based on IP",
  val geolocationPolicy: String = "Based on IP",

  // Storage Isolation
  val saveHistory: Boolean = false,
  val saveBookmarks: Boolean = true,
  val savePasswords: Boolean = false,
  val saveLocalStorage: Boolean = true,
  val enableExtensions: Boolean = false,

  // Hardware Entropy & Specs
  val resolutionWidth: Int = 1080,
  val resolutionHeight: Int = 2400,
  val deviceMemoryGb: Int = 8,
  val cpuThreads: Int = 8,
  val userAgent: String = "",
  val languages: String = "en-US,en;q=0.9",

  // Live Consistency Status
  val consistencyReport: ConsistencyReport? = null,
  val isCreating: Boolean = false,
  val errorMessage: String? = null
)

class CreateProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val folderRepository: FolderRepository,
  private val proxyRepository: ProxyRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateProfileUiState())
  val uiState: StateFlow<CreateProfileUiState> = _uiState.asStateFlow()

  init {
    initializeDefaults()
    observeRepositories()
  }

  private fun initializeDefaults() {
    val initialName = FingerprintGenerator.generateRandomName()
    val fp = FingerprintGenerator.generateConsistentFingerprint("Android", "US")
    _uiState.update { current ->
      current.copy(
        name = initialName,
        os = "Android",
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
        virtualCameraName = fp["vCamName"] as? String ?: current.virtualCameraName
      )
    }
    recalculateConsistency()
  }

  private fun observeRepositories() {
    viewModelScope.launch {
      folderRepository.allFolders.collect { folderList ->
        _uiState.update { it.copy(folders = folderList) }
      }
    }
    viewModelScope.launch {
      proxyRepository.allProxies.collect { proxyList ->
        _uiState.update { it.copy(proxies = proxyList) }
      }
    }
  }

  fun setName(name: String) {
    _uiState.update { it.copy(name = name) }
  }

  fun generateRandomName() {
    _uiState.update { it.copy(name = FingerprintGenerator.generateRandomName()) }
  }

  fun setNote(note: String) {
    _uiState.update { it.copy(note = note) }
  }

  fun setFolder(folderId: String?) {
    _uiState.update { it.copy(folderId = folderId) }
  }

  fun createFolder(name: String, colorHex: String = "#00A98A") {
    if (name.isBlank()) return
    viewModelScope.launch {
      val newFolder = FolderEntity(
        id = "folder_${UUID.randomUUID().toString().take(8)}",
        name = name.trim(),
        colorHex = colorHex
      )
      folderRepository.insertFolder(newFolder)
      _uiState.update { it.copy(folderId = newFolder.id) }
    }
  }

  fun setOs(newOs: String) {
    val country = getActiveCountry()
    val fp = FingerprintGenerator.generateConsistentFingerprint(newOs, country)
    _uiState.update { current ->
      current.copy(
        os = newOs,
        resolutionWidth = fp["resW"] as Int,
        resolutionHeight = fp["resH"] as Int,
        deviceMemoryGb = fp["memory"] as Int,
        cpuThreads = fp["threads"] as Int,
        webGlVendor = fp["gpuVendor"] as String,
        webGlRenderer = fp["gpuRenderer"] as String,
        userAgent = fp["userAgent"] as String,
        virtualCameraName = fp["vCamName"] as? String ?: current.virtualCameraName
      )
    }
    recalculateConsistency()
  }

  fun setProxyMode(mode: ProxyMode) {
    _uiState.update { it.copy(proxyMode = mode) }
    applyLocationSync()
    recalculateConsistency()
  }

  fun selectSavedProxy(proxyId: String?) {
    _uiState.update { it.copy(selectedProxyId = proxyId) }
    applyLocationSync()
    recalculateConsistency()
  }

  fun setCustomProxyProtocol(protocol: String) {
    _uiState.update { it.copy(customProxyProtocol = protocol) }
  }

  fun setCustomProxyHost(host: String) {
    _uiState.update { it.copy(customProxyHost = host) }
  }

  fun setCustomProxyPort(port: String) {
    _uiState.update { it.copy(customProxyPort = port) }
  }

  fun setCustomProxyUsername(user: String) {
    _uiState.update { it.copy(customProxyUsername = user) }
  }

  fun setCustomProxyPassword(pass: String) {
    _uiState.update { it.copy(customProxyPassword = pass) }
  }

  fun setCustomProxyCountry(country: String) {
    _uiState.update { it.copy(customProxyCountry = country.uppercase().trim().take(2)) }
    applyLocationSync()
    recalculateConsistency()
  }

  fun setSaveCustomProxyToPool(save: Boolean) {
    _uiState.update { it.copy(saveCustomProxyToPool = save) }
  }

  fun setAutoMatchLocation(auto: Boolean) {
    _uiState.update { it.copy(autoMatchLocationToProxy = auto) }
    if (auto) {
      applyLocationSync()
    }
    recalculateConsistency()
  }

  fun setTimezone(tz: String) {
    _uiState.update { it.copy(timezone = tz) }
    recalculateConsistency()
  }

  fun setCoordinates(lat: Double, lon: Double) {
    _uiState.update { it.copy(latitude = lat, longitude = lon) }
  }

  fun applyPrivacyPreset(preset: PrivacyPreset) {
    _uiState.update { current ->
      when (preset) {
        PrivacyPreset.MAXIMUM_STEALTH -> current.copy(
          privacyPreset = preset,
          canvasNoiseEnabled = true,
          webGlSpoofing = true,
          audioNoiseEnabled = true,
          virtualCameraEnabled = true,
          webrtcPolicy = "Disabled",
          dnsPolicy = "Based on IP",
          geolocationPolicy = "Based on IP",
          saveHistory = false,
          saveBookmarks = true,
          savePasswords = false,
          saveLocalStorage = false
        )
        PrivacyPreset.BALANCED -> current.copy(
          privacyPreset = preset,
          canvasNoiseEnabled = true,
          webGlSpoofing = true,
          audioNoiseEnabled = true,
          virtualCameraEnabled = true,
          webrtcPolicy = "Proxy IP Only",
          dnsPolicy = "Based on IP",
          geolocationPolicy = "Based on IP",
          saveHistory = false,
          saveBookmarks = true,
          savePasswords = false,
          saveLocalStorage = true
        )
        PrivacyPreset.FAST -> current.copy(
          privacyPreset = preset,
          canvasNoiseEnabled = false,
          webGlSpoofing = false,
          audioNoiseEnabled = false,
          virtualCameraEnabled = false,
          webrtcPolicy = "Proxy IP Only",
          dnsPolicy = "Cloudflare",
          geolocationPolicy = "Based on IP",
          saveHistory = true,
          saveBookmarks = true,
          savePasswords = false,
          saveLocalStorage = true
        )
        PrivacyPreset.CUSTOM -> current.copy(privacyPreset = preset)
      }
    }
    recalculateConsistency()
  }

  fun setCanvasNoise(enabled: Boolean) {
    _uiState.update { it.copy(canvasNoiseEnabled = enabled, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setWebGlSpoofing(enabled: Boolean) {
    _uiState.update { it.copy(webGlSpoofing = enabled, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setAudioNoise(enabled: Boolean) {
    _uiState.update { it.copy(audioNoiseEnabled = enabled, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setVirtualCamera(enabled: Boolean) {
    _uiState.update { it.copy(virtualCameraEnabled = enabled, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setVirtualCameraName(name: String) {
    _uiState.update { it.copy(virtualCameraName = name) }
  }

  fun setVirtualCameraMode(mode: String) {
    _uiState.update { it.copy(virtualCameraMode = mode) }
  }

  fun setVirtualCameraMediaType(mediaType: String) {
    _uiState.update { it.copy(virtualCameraMediaType = mediaType) }
  }

  fun setVirtualCameraMedia(filePath: String, base64: String, mediaType: String, thumbnail: android.graphics.Bitmap?) {
    _uiState.update {
      it.copy(
        virtualCameraMediaType = mediaType,
        virtualCameraMediaUri = filePath,
        virtualCameraMediaBase64 = base64,
        virtualCameraThumbnail = thumbnail
      )
    }
  }

  fun clearVirtualCameraMedia() {
    _uiState.update {
      it.copy(
        virtualCameraMediaType = "SYNTHETIC",
        virtualCameraMediaUri = null,
        virtualCameraMediaBase64 = null,
        virtualCameraThumbnail = null
      )
    }
  }

  fun setWebRtcPolicy(policy: String) {
    _uiState.update { it.copy(webrtcPolicy = policy, privacyPreset = PrivacyPreset.CUSTOM) }
    recalculateConsistency()
  }

  fun setDnsPolicy(policy: String) {
    _uiState.update { it.copy(dnsPolicy = policy, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setGeolocationPolicy(policy: String) {
    _uiState.update { it.copy(geolocationPolicy = policy, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setSaveHistory(save: Boolean) {
    _uiState.update { it.copy(saveHistory = save, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setSaveBookmarks(save: Boolean) {
    _uiState.update { it.copy(saveBookmarks = save, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setSavePasswords(save: Boolean) {
    _uiState.update { it.copy(savePasswords = save, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setSaveLocalStorage(save: Boolean) {
    _uiState.update { it.copy(saveLocalStorage = save, privacyPreset = PrivacyPreset.CUSTOM) }
  }

  fun setResolution(width: Int, height: Int) {
    _uiState.update { it.copy(resolutionWidth = width, resolutionHeight = height) }
    recalculateConsistency()
  }

  fun regenerateEntropy() {
    val state = _uiState.value
    val country = getActiveCountry()
    val fp = FingerprintGenerator.generateConsistentFingerprint(state.os, country)
    _uiState.update { current ->
      current.copy(
        resolutionWidth = fp["resW"] as Int,
        resolutionHeight = fp["resH"] as Int,
        deviceMemoryGb = fp["memory"] as Int,
        cpuThreads = fp["threads"] as Int,
        webGlVendor = fp["gpuVendor"] as String,
        webGlRenderer = fp["gpuRenderer"] as String,
        userAgent = fp["userAgent"] as String,
        languages = fp["languages"] as String,
        virtualCameraName = fp["vCamName"] as? String ?: current.virtualCameraName
      )
    }
    recalculateConsistency()
  }

  private fun getActiveCountry(): String {
    val state = _uiState.value
    return when (state.proxyMode) {
      ProxyMode.DIRECT -> "DIRECT"
      ProxyMode.SAVED -> {
        val proxy = state.proxies.find { it.id == state.selectedProxyId }
        proxy?.countryCode ?: "US"
      }
      ProxyMode.CUSTOM -> {
        if (state.customProxyCountry.isNotBlank()) state.customProxyCountry else "US"
      }
    }
  }

  private fun applyLocationSync() {
    val state = _uiState.value
    if (!state.autoMatchLocationToProxy) return

    val country = getActiveCountry()
    if (country == "DIRECT") {
      _uiState.update {
        it.copy(
          timezone = "Based on IP",
          latitude = 37.7749,
          longitude = -122.4194,
          languages = "en-US,en;q=0.9"
        )
      }
    } else {
      val (tz, coords) = FingerprintGenerator.getTimezoneForCountry(country)
      val langs = FingerprintGenerator.getLanguagesForCountry(country)
      _uiState.update {
        it.copy(
          timezone = tz,
          latitude = coords.first,
          longitude = coords.second,
          languages = langs
        )
      }
    }
  }

  private fun recalculateConsistency() {
    val state = _uiState.value
    val country = getActiveCountry()
    val report = FingerprintGenerator.checkConsistency(
      os = state.os,
      userAgent = state.userAgent,
      proxyCountry = country,
      timezone = state.timezone,
      width = state.resolutionWidth,
      height = state.resolutionHeight,
      gpuVendor = state.webGlVendor,
      gpuRenderer = state.webGlRenderer,
      webrtcPolicy = state.webrtcPolicy
    )
    _uiState.update { it.copy(consistencyReport = report) }
  }

  fun createProfile(onSuccess: (ProfileEntity) -> Unit) {
    val state = _uiState.value
    val finalName = if (state.name.isNotBlank()) state.name.trim() else FingerprintGenerator.generateRandomName()

    viewModelScope.launch {
      _uiState.update { it.copy(isCreating = true, errorMessage = null) }

      var assignedProxyId: String? = null
      var proxyCountry = "DIRECT"

      when (state.proxyMode) {
        ProxyMode.DIRECT -> {
          assignedProxyId = null
          proxyCountry = "DIRECT"
        }
        ProxyMode.SAVED -> {
          assignedProxyId = state.selectedProxyId
          val proxy = state.proxies.find { it.id == assignedProxyId }
          proxyCountry = proxy?.countryCode ?: "DIRECT"
        }
        ProxyMode.CUSTOM -> {
          if (state.customProxyHost.isNotBlank()) {
            val portInt = state.customProxyPort.toIntOrNull() ?: 8080
            val encryptedPass = if (state.customProxyPassword.isNotEmpty()) {
              CryptoManager.encrypt(state.customProxyPassword)
            } else ""
            val newProxy = ProxyEntity(
              id = "proxy_${UUID.randomUUID().toString().take(8)}",
              name = "$finalName Proxy (${state.customProxyProtocol})",
              isRotating = false,
              protocol = state.customProxyProtocol,
              host = state.customProxyHost.trim(),
              port = portInt,
              username = state.customProxyUsername.trim(),
              encryptedPassword = encryptedPass,
              countryCode = state.customProxyCountry.ifBlank { "US" },
              countryName = "Custom Proxy",
              createdAt = System.currentTimeMillis()
            )
            if (state.saveCustomProxyToPool) {
              proxyRepository.insertProxy(newProxy)
            }
            assignedProxyId = newProxy.id
            proxyCountry = newProxy.countryCode
          }
        }
      }

      val summary = buildString {
        append("Isolated ${state.os} Profile")
        if (state.canvasNoiseEnabled) append(" • Canvas Noise")
        if (state.webGlSpoofing) append(" • WebGL Mask")
        if (state.audioNoiseEnabled) append(" • Audio Jitter")
        append(" • $proxyCountry IP")
      }

      val profile = ProfileEntity(
        id = UUID.randomUUID().toString(),
        name = finalName,
        note = state.note.trim(),
        folderId = state.folderId,
        os = state.os,
        proxyId = assignedProxyId,
        proxyCountry = proxyCountry,
        startUrl = "https://browserleaks.com/ip",
        saveHistory = state.saveHistory,
        saveBookmarks = state.saveBookmarks,
        savePasswords = state.savePasswords,
        saveLocalStorage = state.saveLocalStorage,
        enableExtensions = state.enableExtensions,
        resolutionWidth = state.resolutionWidth,
        resolutionHeight = state.resolutionHeight,
        deviceMemoryGb = state.deviceMemoryGb,
        cpuThreads = state.cpuThreads,
        canvasNoiseEnabled = state.canvasNoiseEnabled,
        webGlSpoofing = state.webGlSpoofing,
        webGlVendor = state.webGlVendor,
        webGlRenderer = state.webGlRenderer,
        audioNoiseEnabled = state.audioNoiseEnabled,
        virtualCameraEnabled = state.virtualCameraEnabled,
        virtualCameraName = state.virtualCameraName,
        virtualCameraMode = state.virtualCameraMode,
        virtualCameraFps = state.virtualCameraFps,
        virtualCameraMediaType = state.virtualCameraMediaType,
        virtualCameraMediaUri = state.virtualCameraMediaUri,
        virtualCameraMediaBase64 = state.virtualCameraMediaBase64,
        timezone = state.timezone,
        geolocationPolicy = state.geolocationPolicy,
        latitude = state.latitude,
        longitude = state.longitude,
        webrtcPolicy = state.webrtcPolicy,
        dnsPolicy = state.dnsPolicy,
        userAgent = state.userAgent,
        languages = state.languages,
        fingerprintSummary = summary,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )

      profileRepository.insertProfile(profile)
      _uiState.update { it.copy(isCreating = false) }
      onSuccess(profile)
    }
  }

  class Factory(
    private val profileRepository: ProfileRepository,
    private val folderRepository: FolderRepository,
    private val proxyRepository: ProxyRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CreateProfileViewModel(
        profileRepository,
        folderRepository,
        proxyRepository
      ) as T
    }
  }
}
