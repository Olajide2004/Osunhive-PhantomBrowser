package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val id: String,
  val name: String,
  val note: String = "",
  val folderId: String? = null,
  val os: String = "Android", // Android, Windows, macOS, Linux, iOS
  val proxyId: String? = null,
  val proxyCountry: String = "DIRECT",
  val dataUsedMb: Double = 0.0,
  val isRunning: Boolean = false,
  
  // Browser settings
  val startUrl: String = "https://browserleaks.com/ip",
  val launchArguments: String = "--disable-blink-features=AutomationControlled",
  val languages: String = "en-US,en;q=0.9",
  val customFonts: String = "Roboto,Arial,sans-serif",
  
  // Storage settings
  val saveHistory: Boolean = false,
  val saveBookmarks: Boolean = true,
  val savePasswords: Boolean = false,
  val saveLocalStorage: Boolean = true,
  val enableExtensions: Boolean = false,
  
  // Hardware & Fingerprint
  val resolutionWidth: Int = 1080,
  val resolutionHeight: Int = 2400,
  val deviceMemoryGb: Int = 8,
  val cpuThreads: Int = 8,
  val canvasNoiseEnabled: Boolean = true,
  val webGlSpoofing: Boolean = true,
  val webGlVendor: String = "Qualcomm",
  val webGlRenderer: String = "Adreno (TM) 750",
  val audioNoiseEnabled: Boolean = true,
  val mediaDeviceCount: Int = 3,
  
  // Virtual Camera Emulation
  val virtualCameraEnabled: Boolean = true,
  val virtualCameraName: String = "Logitech HD Pro Webcam C920",
  val virtualCameraMode: String = "Color Bars & Timer",
  val virtualCameraFps: Int = 30,
  val virtualCameraMediaType: String = "SYNTHETIC", // "SYNTHETIC", "IMAGE", "VIDEO"
  val virtualCameraMediaUri: String? = null,
  val virtualCameraMediaBase64: String? = null,
  
  // Proxy & Location
  val timezone: String = "Based on IP",
  val geolocationPolicy: String = "Based on IP",
  val latitude: Double = 37.7749,
  val longitude: Double = -122.4194,
  val accuracyMeters: Double = 25.0,
  val webrtcPolicy: String = "Proxy IP Only",
  val dnsPolicy: String = "Based on IP",
  
  // Generated identifiers
  val userAgent: String = "",
  val fingerprintSummary: String = "Standard Stealth Profile",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
