package com.example

import com.example.data.fingerprint.FingerprintGenerator
import com.example.ui.profiles.create.PrivacyPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateProfileTest {

  @Test
  fun testRandomNameGeneration() {
    val name1 = FingerprintGenerator.generateRandomName()
    val name2 = FingerprintGenerator.generateRandomName()
    assertTrue(name1.isNotEmpty())
    assertTrue(name1.contains("-"))
    assertNotNull(name2)
  }

  @Test
  fun testOsFingerprintGeneration() {
    val winFp = FingerprintGenerator.generateConsistentFingerprint("Windows", "US")
    assertEquals(1920, winFp["resW"])
    assertEquals(1080, winFp["resH"])
    assertTrue((winFp["userAgent"] as String).contains("Windows NT 10.0"))

    val macFp = FingerprintGenerator.generateConsistentFingerprint("macOS", "US")
    assertTrue((macFp["userAgent"] as String).contains("Macintosh"))

    val androidFp = FingerprintGenerator.generateConsistentFingerprint("Android", "US")
    assertTrue((androidFp["userAgent"] as String).contains("Android"))
  }

  @Test
  fun testConsistencyValidation() {
    val winUa = FingerprintGenerator.generateUserAgent("Windows")
    val report = FingerprintGenerator.checkConsistency(
      os = "Windows",
      userAgent = winUa,
      proxyCountry = "US",
      timezone = "America/New_York",
      width = 1920,
      height = 1080,
      gpuVendor = "Google Inc. (NVIDIA)",
      gpuRenderer = "ANGLE (NVIDIA)",
      webrtcPolicy = "Proxy IP Only"
    )

    // With matching OS, resolution, and UA, the risk should be low
    assertTrue(report.riskScore <= 0.20f)
    assertEquals("Low Detection Risk", report.riskLabel)
    assertTrue(report.warnings.isEmpty())
  }

  @Test
  fun testPrivacyPresetDefinitions() {
    val presets = PrivacyPreset.values()
    assertTrue(presets.contains(PrivacyPreset.MAXIMUM_STEALTH))
    assertTrue(presets.contains(PrivacyPreset.BALANCED))
    assertTrue(presets.contains(PrivacyPreset.FAST))
    assertTrue(presets.contains(PrivacyPreset.CUSTOM))
  }
}
