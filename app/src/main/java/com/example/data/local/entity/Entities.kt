package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxies")
data class ProxyEntity(
  @PrimaryKey val id: String,
  val name: String,
  val isRotating: Boolean = false,
  val protocol: String = "HTTP", // HTTP, HTTPS, SOCKS5
  val host: String = "",
  val port: Int = 8080,
  val username: String = "",
  val encryptedPassword: String = "",
  val countryCode: String = "US",
  val countryName: String = "United States",
  val ipPoolType: String = "Residential", // Residential, Datacenter
  val rotationIntervalMinutes: Int = 10,
  val isFavorite: Boolean = false,
  val latencyMs: Long = -1L,
  val lastTestedTime: Long = 0L,
  val lastStatus: String = "Untested", // Active, Failed, Untested
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class FolderEntity(
  @PrimaryKey val id: String,
  val name: String,
  val colorHex: String = "#00A98A",
  val iconName: String = "folder",
  val orderIndex: Int = 0
)

@Entity(tableName = "fingerprint_presets")
data class FingerprintPresetEntity(
  @PrimaryKey val id: String,
  val title: String,
  val os: String,
  val deviceModel: String,
  val browserVersion: String,
  val userAgent: String,
  val resolutionWidth: Int,
  val resolutionHeight: Int,
  val gpuVendor: String,
  val gpuRenderer: String,
  val cpuThreads: Int,
  val memoryGb: Int,
  val languages: String
)
