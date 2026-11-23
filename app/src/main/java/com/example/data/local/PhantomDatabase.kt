package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FingerprintPresetDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ProxyDao
import com.example.data.local.entity.FingerprintPresetEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ProfileEntity::class,
    ProxyEntity::class,
    FolderEntity::class,
    FingerprintPresetEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class PhantomDatabase : RoomDatabase() {
  abstract fun profileDao(): ProfileDao
  abstract fun proxyDao(): ProxyDao
  abstract fun folderDao(): FolderDao
  abstract fun fingerprintPresetDao(): FingerprintPresetDao

  companion object {
    @Volatile
    private var INSTANCE: PhantomDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): PhantomDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PhantomDatabase::class.java,
          "phantom_profile_db"
        )
          .addCallback(PhantomDatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class PhantomDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: PhantomDatabase) {
      val folderDao = database.folderDao()
      val proxyDao = database.proxyDao()
      val presetDao = database.fingerprintPresetDao()
      val profileDao = database.profileDao()

      // Initial Folders
      val defaultFolders = listOf(
        FolderEntity("folder_social", "Social Media", "#00A98A", "people", 0),
        FolderEntity("folder_ecom", "E-Commerce", "#0EA5E9", "shopping_cart", 1),
        FolderEntity("folder_crypto", "Crypto & Web3", "#F59E0B", "currency_bitcoin", 2),
        FolderEntity("folder_qa", "QA / Automation", "#8B5CF6", "bug_report", 3)
      )
      folderDao.insertFolders(defaultFolders)

      // Initial Custom Proxies Pool across all supported protocols
      val initialCustomProxies = listOf(
        ProxyEntity(
          id = "custom_socks5_us",
          name = "US Residential SOCKS5",
          isRotating = false,
          protocol = "SOCKS5",
          host = "104.28.19.45",
          port = 1080,
          username = "stealth_user",
          encryptedPassword = "",
          countryCode = "US",
          countryName = "United States",
          ipPoolType = "Residential",
          isFavorite = true,
          latencyMs = 38,
          lastStatus = "Active"
        ),
        ProxyEntity(
          id = "custom_http_de",
          name = "Frankfurt HTTP Proxy",
          isRotating = false,
          protocol = "HTTP",
          host = "185.190.140.22",
          port = 8080,
          username = "",
          encryptedPassword = "",
          countryCode = "DE",
          countryName = "Germany",
          ipPoolType = "Datacenter",
          isFavorite = true,
          latencyMs = 45,
          lastStatus = "Active"
        ),
        ProxyEntity(
          id = "custom_https_gb",
          name = "London SSL/HTTPS Secure",
          isRotating = false,
          protocol = "HTTPS",
          host = "51.159.20.12",
          port = 8443,
          username = "london_ops",
          encryptedPassword = "",
          countryCode = "GB",
          countryName = "United Kingdom",
          ipPoolType = "Residential",
          isFavorite = false,
          latencyMs = 62,
          lastStatus = "Active"
        ),
        ProxyEntity(
          id = "custom_socks4_jp",
          name = "Tokyo SOCKS4 Mobile Node",
          isRotating = false,
          protocol = "SOCKS4",
          host = "133.242.18.99",
          port = 1080,
          username = "",
          encryptedPassword = "",
          countryCode = "JP",
          countryName = "Japan",
          ipPoolType = "Mobile 4G/5G",
          isFavorite = false,
          latencyMs = 110,
          lastStatus = "Active"
        ),
        ProxyEntity(
          id = "custom_shadowsocks_sg",
          name = "Singapore Shadowsocks",
          isRotating = false,
          protocol = "Shadowsocks",
          host = "128.199.200.5",
          port = 8388,
          username = "aes-256-gcm",
          encryptedPassword = "",
          countryCode = "SG",
          countryName = "Singapore",
          ipPoolType = "Datacenter",
          isFavorite = true,
          latencyMs = 95,
          lastStatus = "Active"
        )
      )
      proxyDao.insertProxies(initialCustomProxies)

      // Preloaded Device Fingerprints
      val devicePresets = listOf(
        FingerprintPresetEntity(
          id = "preset_pixel8",
          title = "Google Pixel 8 Pro",
          os = "Android",
          deviceModel = "Pixel 8 Pro",
          browserVersion = "Chrome 128.0.6613.88",
          userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36",
          resolutionWidth = 1080,
          resolutionHeight = 2400,
          gpuVendor = "Google Inc. (ARM)",
          gpuRenderer = "Mali-G715 Immortalis MC10",
          cpuThreads = 8,
          memoryGb = 12,
          languages = "en-US,en;q=0.9"
        ),
        FingerprintPresetEntity(
          id = "preset_s24",
          title = "Samsung Galaxy S24 Ultra",
          os = "Android",
          deviceModel = "SM-S928B",
          browserVersion = "Chrome 128.0.6613.88",
          userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36",
          resolutionWidth = 1440,
          resolutionHeight = 3120,
          gpuVendor = "Qualcomm",
          gpuRenderer = "Adreno (TM) 750",
          cpuThreads = 8,
          memoryGb = 12,
          languages = "en-US,en;q=0.9"
        ),
        FingerprintPresetEntity(
          id = "preset_win11_rtx",
          title = "Windows 11 Chrome Desktop",
          os = "Windows",
          deviceModel = "Desktop PC",
          browserVersion = "Chrome 128.0.6613.114",
          userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36",
          resolutionWidth = 1920,
          resolutionHeight = 1080,
          gpuVendor = "Google Inc. (NVIDIA)",
          gpuRenderer = "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)",
          cpuThreads = 16,
          memoryGb = 16,
          languages = "en-US,en;q=0.9"
        ),
        FingerprintPresetEntity(
          id = "preset_mac_m3",
          title = "macOS Sonoma (M3 Pro)",
          os = "macOS",
          deviceModel = "MacBookPro18,1",
          browserVersion = "Chrome 128.0.6613.114",
          userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36",
          resolutionWidth = 1728,
          resolutionHeight = 1117,
          gpuVendor = "Apple Inc.",
          gpuRenderer = "Apple M3 Pro",
          cpuThreads = 12,
          memoryGb = 18,
          languages = "en-US,en;q=0.9"
        ),
        FingerprintPresetEntity(
          id = "preset_ubuntu",
          title = "Ubuntu 24.04 Linux",
          os = "Linux",
          deviceModel = "Linux Workstation",
          browserVersion = "Chrome 128.0.6613.85",
          userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.85 Safari/537.36",
          resolutionWidth = 1920,
          resolutionHeight = 1080,
          gpuVendor = "Mesa/Intel",
          gpuRenderer = "Mesa Intel(R) Iris(R) Xe Graphics (TGL GT2)",
          cpuThreads = 8,
          memoryGb = 16,
          languages = "en-US,en;q=0.9"
        ),
        FingerprintPresetEntity(
          id = "preset_iphone15",
          title = "iPhone 15 Pro (iOS 17.5)",
          os = "iOS",
          deviceModel = "iPhone16,1",
          browserVersion = "Mobile Safari 17.5",
          userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
          resolutionWidth = 1179,
          resolutionHeight = 2556,
          gpuVendor = "Apple Inc.",
          gpuRenderer = "Apple A17 Pro GPU",
          cpuThreads = 6,
          memoryGb = 8,
          languages = "en-US,en;q=0.9"
        )
      )
      presetDao.insertPresets(devicePresets)

      // Initial Default Profiles to showcase right away
      val initialProfiles = listOf(
        ProfileEntity(
          id = "prof_gentle_silence",
          name = "gentle-silence",
          note = "Primary stealth research profile with US fingerprint",
          folderId = "folder_social",
          os = "Android",
          proxyId = null,
          proxyCountry = "US",
          dataUsedMb = 34.6,
          isRunning = false,
          startUrl = "https://browserleaks.com/ip",
          resolutionWidth = 1080,
          resolutionHeight = 2400,
          deviceMemoryGb = 8,
          cpuThreads = 8,
          canvasNoiseEnabled = true,
          webGlSpoofing = true,
          webGlVendor = "Qualcomm",
          webGlRenderer = "Adreno (TM) 750",
          audioNoiseEnabled = true,
          timezone = "America/New_York",
          geolocationPolicy = "Based on IP",
          latitude = 40.7128,
          longitude = -74.0060,
          webrtcPolicy = "Proxy IP Only",
          dnsPolicy = "Based on IP",
          userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36",
          fingerprintSummary = "High Stealth • US IP • Pixel 8 Pro Fingerprint"
        ),
        ProfileEntity(
          id = "prof_stealth_falcon",
          name = "stealth-falcon",
          note = "Frankfurt high-privacy desktop simulation session",
          folderId = "folder_ecom",
          os = "Windows",
          proxyId = null,
          proxyCountry = "DE",
          dataUsedMb = 18.2,
          isRunning = false,
          startUrl = "https://amiunique.org/fingerprint",
          resolutionWidth = 1920,
          resolutionHeight = 1080,
          deviceMemoryGb = 16,
          cpuThreads = 16,
          canvasNoiseEnabled = true,
          webGlSpoofing = true,
          webGlVendor = "Google Inc. (NVIDIA)",
          webGlRenderer = "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)",
          audioNoiseEnabled = true,
          timezone = "Europe/Berlin",
          geolocationPolicy = "Based on IP",
          latitude = 50.1109,
          longitude = 8.6821,
          webrtcPolicy = "Disabled",
          dnsPolicy = "Cloudflare",
          userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36",
          fingerprintSummary = "Max Stealth • DE Datacenter • Win11 RTX 4070"
        ),
        ProfileEntity(
          id = "prof_amber_voyage",
          name = "amber-voyage",
          note = "Clean direct profile with canvas & audio noise spoofing",
          folderId = "folder_qa",
          os = "macOS",
          proxyId = null,
          proxyCountry = "DIRECT",
          dataUsedMb = 6.4,
          isRunning = false,
          startUrl = "https://browserleaks.com/canvas",
          resolutionWidth = 1728,
          resolutionHeight = 1117,
          deviceMemoryGb = 18,
          cpuThreads = 12,
          canvasNoiseEnabled = true,
          webGlSpoofing = true,
          webGlVendor = "Apple Inc.",
          webGlRenderer = "Apple M3 Pro",
          audioNoiseEnabled = true,
          timezone = "Based on IP",
          geolocationPolicy = "Based on IP",
          webrtcPolicy = "Proxy IP Only",
          dnsPolicy = "Based on IP",
          userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36",
          fingerprintSummary = "Direct Connection • macOS M3 Pro • Noise Injected"
        )
      )
      profileDao.insertProfiles(initialProfiles)
    }
  }
}
