package com.example.data.fingerprint

import java.util.UUID
import kotlin.random.Random

data class ConsistencyReport(
  val riskScore: Float, // 0.0 (Perfect) to 1.0 (High Risk)
  val riskLabel: String, // "Low Risk", "Medium Risk", "High Risk"
  val warnings: List<String>,
  val recommendations: List<String>
)

object FingerprintGenerator {
  private val adjectives = listOf(
    "gentle", "swift", "silent", "amber", "iron", "velvet", "frost", "crimson",
    "solar", "neon", "shadow", "cyber", "lunar", "arctic", "stealth", "crystal",
    "hollow", "quiet", "phantom", "echo", "vivid", "prism", "zenith", "cobalt"
  )

  private val nouns = listOf(
    "silence", "falcon", "voyage", "circuit", "echo", "breeze", "whisper", "horizon",
    "shield", "vortex", "matrix", "stream", "spark", "beacon", "haven", "mirage",
    "orbit", "tide", "pulse", "summit", "drift", "nexus", "core", "harbor"
  )

  fun generateRandomName(): String {
    val adj = adjectives.random()
    val noun = nouns.random()
    return "$adj-$noun"
  }

  fun generateUserAgent(os: String, chromeVer: String = "128.0.6613.114"): String {
    return when (os) {
      "Windows" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
      "macOS" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
      "Linux" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
      "iOS" -> "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
      else -> "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Mobile Safari/537.36"
    }
  }

  fun getCameraForOs(os: String): String {
    return when (os) {
      "macOS" -> "FaceTime HD Camera (Built-in)"
      "Windows" -> "Logitech Brio 4K Stream Edition"
      "Linux" -> "Integrated Webcam (USB 2.0)"
      "iOS" -> "Front TrueDepth Camera"
      else -> "Front Wide-Angle Camera"
    }
  }

  fun getPlatformForOs(os: String): String {
    return when (os) {
      "Windows" -> "Win32"
      "macOS" -> "MacIntel"
      "Linux" -> "Linux x86_64"
      "iOS" -> "iPhone"
      else -> "Linux armv81"
    }
  }

  fun getTimezoneForCountry(countryCode: String): Pair<String, Pair<Double, Double>> {
    return when (countryCode.uppercase()) {
      "US" -> Pair("America/New_York", Pair(40.7128, -74.0060))
      "DE" -> Pair("Europe/Berlin", Pair(52.5200, 13.4050))
      "GB" -> Pair("Europe/London", Pair(51.5074, -0.1278))
      "JP" -> Pair("Asia/Tokyo", Pair(35.6762, 139.6503))
      "CA" -> Pair("America/Toronto", Pair(43.6532, -79.3832))
      "SG" -> Pair("Asia/Singapore", Pair(1.3521, 103.8198))
      "FR" -> Pair("Europe/Paris", Pair(48.8566, 2.3522))
      "NL" -> Pair("Europe/Amsterdam", Pair(52.3676, 4.9041))
      "AU" -> Pair("Australia/Sydney", Pair(-33.8688, 151.2093))
      else -> Pair("UTC", Pair(0.0, 0.0))
    }
  }

  fun getLanguagesForCountry(countryCode: String): String {
    return when (countryCode.uppercase()) {
      "US" -> "en-US,en;q=0.9"
      "DE" -> "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7"
      "GB" -> "en-GB,en;q=0.9,en-US;q=0.8"
      "JP" -> "ja-JP,ja;q=0.9,en-US;q=0.8,en;q=0.7"
      "CA" -> "en-CA,en-US;q=0.9,fr-CA;q=0.8"
      "SG" -> "en-SG,en;q=0.9,zh-SG;q=0.8"
      "FR" -> "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"
      "NL" -> "nl-NL,nl;q=0.9,en-US;q=0.8,en;q=0.7"
      "AU" -> "en-AU,en;q=0.9,en-US;q=0.8"
      else -> "en-US,en;q=0.9"
    }
  }

  fun generateConsistentFingerprint(os: String, proxyCountry: String): Map<String, Any> {
    val country = if (proxyCountry == "DIRECT" || proxyCountry.isBlank()) "US" else proxyCountry
    val (tz, coords) = getTimezoneForCountry(country)
    val langs = getLanguagesForCountry(country)

    return when (os) {
      "Windows" -> mapOf(
        "resW" to 1920,
        "resH" to 1080,
        "memory" to 16,
        "threads" to 16,
        "gpuVendor" to "Google Inc. (NVIDIA)",
        "gpuRenderer" to "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)",
        "timezone" to tz,
        "latitude" to coords.first,
        "longitude" to coords.second,
        "languages" to langs,
        "userAgent" to generateUserAgent("Windows"),
        "vCamName" to getCameraForOs("Windows")
      )
      "macOS" -> mapOf(
        "resW" to 1728,
        "resH" to 1117,
        "memory" to 18,
        "threads" to 12,
        "gpuVendor" to "Apple Inc.",
        "gpuRenderer" to "Apple M3 Pro",
        "timezone" to tz,
        "latitude" to coords.first,
        "longitude" to coords.second,
        "languages" to langs,
        "userAgent" to generateUserAgent("macOS"),
        "vCamName" to getCameraForOs("macOS")
      )
      "Linux" -> mapOf(
        "resW" to 1920,
        "resH" to 1080,
        "memory" to 16,
        "threads" to 8,
        "gpuVendor" to "Mesa/Intel",
        "gpuRenderer" to "Mesa Intel(R) Iris(R) Xe Graphics (TGL GT2)",
        "timezone" to tz,
        "latitude" to coords.first,
        "longitude" to coords.second,
        "languages" to langs,
        "userAgent" to generateUserAgent("Linux"),
        "vCamName" to getCameraForOs("Linux")
      )
      "iOS" -> mapOf(
        "resW" to 1179,
        "resH" to 2556,
        "memory" to 8,
        "threads" to 6,
        "gpuVendor" to "Apple Inc.",
        "gpuRenderer" to "Apple A17 Pro GPU",
        "timezone" to tz,
        "latitude" to coords.first,
        "longitude" to coords.second,
        "languages" to langs,
        "userAgent" to generateUserAgent("iOS"),
        "vCamName" to getCameraForOs("iOS")
      )
      else -> mapOf(
        "resW" to 1080,
        "resH" to 2400,
        "memory" to 8,
        "threads" to 8,
        "gpuVendor" to "Qualcomm",
        "gpuRenderer" to "Adreno (TM) 750",
        "timezone" to tz,
        "latitude" to coords.first,
        "longitude" to coords.second,
        "languages" to langs,
        "userAgent" to generateUserAgent("Android"),
        "vCamName" to getCameraForOs("Android")
      )
    }
  }

  fun checkConsistency(
    os: String,
    userAgent: String,
    proxyCountry: String,
    timezone: String,
    width: Int,
    height: Int,
    gpuVendor: String,
    gpuRenderer: String,
    webrtcPolicy: String
  ): ConsistencyReport {
    val warnings = mutableListOf<String>()
    val recommendations = mutableListOf<String>()
    var penalty = 0.0f

    // 1. OS vs User-Agent
    val uaLower = userAgent.lowercase()
    when (os) {
      "Windows" -> if (!uaLower.contains("windows nt")) {
        warnings.add("OS is Windows but User-Agent indicates a different system.")
        penalty += 0.35f
      }
      "macOS" -> if (!uaLower.contains("macintosh") && !uaLower.contains("mac os x")) {
        warnings.add("OS is macOS but User-Agent indicates a different system.")
        penalty += 0.35f
      }
      "Android" -> if (!uaLower.contains("android")) {
        warnings.add("OS is Android but User-Agent doesn't contain Android identifier.")
        penalty += 0.35f
      }
      "iOS" -> if (!uaLower.contains("iphone") && !uaLower.contains("ipad")) {
        warnings.add("OS is iOS but User-Agent does not indicate an iOS device.")
        penalty += 0.35f
      }
      "Linux" -> if (!uaLower.contains("linux") || uaLower.contains("android")) {
        warnings.add("OS is Linux desktop but User-Agent has conflicting signatures.")
        penalty += 0.30f
      }
    }

    // 2. Viewport Aspect Ratio vs OS
    val isPortrait = height > width
    if ((os == "Windows" || os == "macOS" || os == "Linux") && isPortrait) {
      warnings.add("Desktop OS ($os) configured with mobile portrait resolution (${width}x${height}).")
      penalty += 0.20f
    } else if ((os == "Android" || os == "iOS") && !isPortrait && width > 1600) {
      warnings.add("Mobile device configured with ultra-wide desktop resolution (${width}x${height}).")
      penalty += 0.15f
    }

    // 3. GPU vs OS Consistency
    if (os == "macOS" && (gpuVendor.contains("Qualcomm") || gpuRenderer.contains("Adreno"))) {
      warnings.add("Apple macOS device with Qualcomm/Adreno GPU is an impossible hardware combo.")
      penalty += 0.40f
    }
    if (os == "Windows" && gpuRenderer.contains("Apple M")) {
      warnings.add("Windows system with Apple Silicon GPU renderer creates high detection risk.")
      penalty += 0.40f
    }

    // 4. Timezone vs Proxy Country
    if (proxyCountry != "DIRECT" && proxyCountry.isNotBlank() && timezone != "Based on IP") {
      val (expectedTzPrefix, _) = getTimezoneForCountry(proxyCountry)
      val countryZone = expectedTzPrefix.substringBefore("/")
      if (!timezone.startsWith(countryZone)) {
        warnings.add("Timezone ($timezone) does not align with Proxy Country ($proxyCountry).")
        penalty += 0.25f
        recommendations.add("Set Timezone to 'Based on IP' or matching country zone.")
      }
    }

    // 5. WebRTC leak policy check
    if (webrtcPolicy == "Real" && proxyCountry != "DIRECT") {
      warnings.add("WebRTC is set to 'Real' while proxy is active — danger of direct IP leak!")
      penalty += 0.30f
      recommendations.add("Switch WebRTC policy to 'Proxy IP Only' or 'Disabled'.")
    }

    val riskScore = penalty.coerceIn(0.02f, 0.98f)
    val riskLabel = when {
      riskScore <= 0.20f -> "Low Detection Risk"
      riskScore <= 0.50f -> "Medium Risk (Noticeable Discrepancies)"
      else -> "High Risk (Inconsistent Fingerprint)"
    }

    if (warnings.isEmpty()) {
      recommendations.add("Fingerprint and hardware specifications are completely consistent.")
    }

    return ConsistencyReport(
      riskScore = riskScore,
      riskLabel = riskLabel,
      warnings = warnings,
      recommendations = recommendations
    )
  }
}
