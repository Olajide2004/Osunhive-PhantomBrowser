package com.example.data.repository

import com.example.data.local.dao.FingerprintPresetDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ProxyDao
import com.example.data.local.entity.FingerprintPresetEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ProfileRepository(
  private val profileDao: ProfileDao,
  private val presetDao: FingerprintPresetDao
) {
  val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()
  val profilesCount: Flow<Int> = profileDao.getProfilesCount()
  val allPresets: Flow<List<FingerprintPresetEntity>> = presetDao.getAllPresets()

  fun getProfilesByFolder(folderId: String): Flow<List<ProfileEntity>> =
    profileDao.getProfilesByFolder(folderId)

  suspend fun getProfileById(id: String): ProfileEntity? = profileDao.getProfileById(id)

  suspend fun insertProfile(profile: ProfileEntity) = profileDao.insertProfile(profile)

  suspend fun updateProfile(profile: ProfileEntity) = profileDao.updateProfile(profile)

  suspend fun setRunningState(id: String, isRunning: Boolean) = profileDao.updateRunningState(id, isRunning)

  suspend fun stopAllProfiles() = profileDao.stopAllProfiles()

  suspend fun addDataUsage(id: String, deltaMb: Double) = profileDao.addDataUsed(id, deltaMb)

  suspend fun deleteProfile(id: String) = profileDao.deleteProfileById(id)

  suspend fun exportProfilesAsJson(): String {
    val profiles = mutableListOf<ProfileEntity>()
    // Read from DAO
    // For export, we can convert list to JSON
    // We can fetch via snapshot
    // Or we provide a helper to serialize a list of profiles
    return ""
  }

  fun serializeProfilesToJson(profiles: List<ProfileEntity>): String {
    val root = JSONObject()
    root.put("version", 1)
    root.put("exportedAt", System.currentTimeMillis())
    root.put("appName", "PhantomProfile")
    val array = JSONArray()
    for (p in profiles) {
      val obj = JSONObject()
      obj.put("id", p.id)
      obj.put("name", p.name)
      obj.put("note", p.note)
      obj.put("os", p.os)
      obj.put("proxyId", p.proxyId ?: "")
      obj.put("proxyCountry", p.proxyCountry)
      obj.put("startUrl", p.startUrl)
      obj.put("languages", p.languages)
      obj.put("resolutionWidth", p.resolutionWidth)
      obj.put("resolutionHeight", p.resolutionHeight)
      obj.put("deviceMemoryGb", p.deviceMemoryGb)
      obj.put("cpuThreads", p.cpuThreads)
      obj.put("canvasNoiseEnabled", p.canvasNoiseEnabled)
      obj.put("webGlSpoofing", p.webGlSpoofing)
      obj.put("webGlVendor", p.webGlVendor)
      obj.put("webGlRenderer", p.webGlRenderer)
      obj.put("audioNoiseEnabled", p.audioNoiseEnabled)
      obj.put("virtualCameraEnabled", p.virtualCameraEnabled)
      obj.put("virtualCameraName", p.virtualCameraName)
      obj.put("virtualCameraMode", p.virtualCameraMode)
      obj.put("virtualCameraFps", p.virtualCameraFps)
      obj.put("virtualCameraMediaType", p.virtualCameraMediaType)
      obj.put("virtualCameraMediaUri", p.virtualCameraMediaUri ?: "")
      obj.put("virtualCameraMediaBase64", p.virtualCameraMediaBase64 ?: "")
      obj.put("timezone", p.timezone)
      obj.put("geolocationPolicy", p.geolocationPolicy)
      obj.put("latitude", p.latitude)
      obj.put("longitude", p.longitude)
      obj.put("webrtcPolicy", p.webrtcPolicy)
      obj.put("dnsPolicy", p.dnsPolicy)
      obj.put("userAgent", p.userAgent)
      obj.put("fingerprintSummary", p.fingerprintSummary)
      array.put(obj)
    }
    root.put("profiles", array)
    return root.toString(2)
  }

  suspend fun importProfilesFromJson(jsonString: String): Result<Int> {
    return try {
      val root = JSONObject(jsonString)
      val array = root.optJSONArray("profiles") ?: JSONArray()
      val list = mutableListOf<ProfileEntity>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val profile = ProfileEntity(
          id = if (obj.has("id")) obj.getString("id") else UUID.randomUUID().toString(),
          name = obj.optString("name", "imported-profile-$i"),
          note = obj.optString("note", "Imported from JSON"),
          os = obj.optString("os", "Android"),
          proxyId = if (obj.optString("proxyId").isEmpty()) null else obj.optString("proxyId"),
          proxyCountry = obj.optString("proxyCountry", "DIRECT"),
          startUrl = obj.optString("startUrl", "https://browserleaks.com/ip"),
          languages = obj.optString("languages", "en-US,en;q=0.9"),
          resolutionWidth = obj.optInt("resolutionWidth", 1080),
          resolutionHeight = obj.optInt("resolutionHeight", 2400),
          deviceMemoryGb = obj.optInt("deviceMemoryGb", 8),
          cpuThreads = obj.optInt("cpuThreads", 8),
          canvasNoiseEnabled = obj.optBoolean("canvasNoiseEnabled", true),
          webGlSpoofing = obj.optBoolean("webGlSpoofing", true),
          webGlVendor = obj.optString("webGlVendor", "Qualcomm"),
          webGlRenderer = obj.optString("webGlRenderer", "Adreno (TM) 750"),
          audioNoiseEnabled = obj.optBoolean("audioNoiseEnabled", true),
          virtualCameraEnabled = obj.optBoolean("virtualCameraEnabled", true),
          virtualCameraName = obj.optString("virtualCameraName", "Logitech HD Pro Webcam C920"),
          virtualCameraMode = obj.optString("virtualCameraMode", "Color Bars & Timer"),
          virtualCameraFps = obj.optInt("virtualCameraFps", 30),
          virtualCameraMediaType = obj.optString("virtualCameraMediaType", "SYNTHETIC"),
          virtualCameraMediaUri = if (obj.optString("virtualCameraMediaUri").isEmpty()) null else obj.optString("virtualCameraMediaUri"),
          virtualCameraMediaBase64 = if (obj.optString("virtualCameraMediaBase64").isEmpty()) null else obj.optString("virtualCameraMediaBase64"),
          timezone = obj.optString("timezone", "Based on IP"),
          geolocationPolicy = obj.optString("geolocationPolicy", "Based on IP"),
          latitude = obj.optDouble("latitude", 37.7749),
          longitude = obj.optDouble("longitude", -122.4194),
          webrtcPolicy = obj.optString("webrtcPolicy", "Proxy IP Only"),
          dnsPolicy = obj.optString("dnsPolicy", "Based on IP"),
          userAgent = obj.optString("userAgent", ""),
          fingerprintSummary = obj.optString("fingerprintSummary", "Imported Profile"),
          updatedAt = System.currentTimeMillis()
        )
        list.add(profile)
      }
      profileDao.insertProfiles(list)
      Result.success(list.size)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

class ProxyRepository(private val proxyDao: ProxyDao) {
  val allProxies: Flow<List<ProxyEntity>> = proxyDao.getAllProxies()
  val rotatingProxies: Flow<List<ProxyEntity>> = proxyDao.getRotatingProxies()
  val customProxies: Flow<List<ProxyEntity>> = proxyDao.getCustomProxies()

  suspend fun getProxyById(id: String): ProxyEntity? = proxyDao.getProxyById(id)

  suspend fun insertProxy(proxy: ProxyEntity) = proxyDao.insertProxy(proxy)

  suspend fun updateProxy(proxy: ProxyEntity) = proxyDao.updateProxy(proxy)

  suspend fun deleteProxy(id: String) = proxyDao.deleteProxyById(id)

  suspend fun updateLatency(id: String, latency: Long, status: String) {
    proxyDao.updateLatency(id, latency, status, System.currentTimeMillis())
  }

  suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
    proxyDao.updateFavorite(id, isFavorite)
  }
}

class FolderRepository(private val folderDao: FolderDao) {
  val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

  suspend fun insertFolder(folder: FolderEntity) = folderDao.insertFolder(folder)

  suspend fun deleteFolder(id: String) = folderDao.deleteFolderById(id)
}
