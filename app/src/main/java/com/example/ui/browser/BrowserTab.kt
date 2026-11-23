package com.example.ui.browser

import java.util.UUID

data class BrowserTab(
  val id: String = UUID.randomUUID().toString(),
  var url: String = "chrome://newtab",
  var title: String = "New Tab",
  var faviconEmoji: String = "🌐",
  var isDesktopMode: Boolean = false,
  var historyBack: List<String> = emptyList(),
  var historyForward: List<String> = emptyList()
)

data class SpeedDialItem(
  val title: String,
  val url: String,
  val iconEmoji: String,
  val category: String = "Stealth Check"
)

val DEFAULT_SPEED_DIALS = listOf(
  SpeedDialItem("Osunhive", "https://www.osunhive.name.ng", "🐝", "Portal"),
  SpeedDialItem("My IP & Proxy", "https://browserleaks.com/ip", "🌐", "Stealth Check"),
  SpeedDialItem("Canvas Test", "https://browserleaks.com/canvas", "🎨", "Stealth Check"),
  SpeedDialItem("AmIUnique", "https://amiunique.org/fingerprint", "🛡️", "Stealth Check"),
  SpeedDialItem("WebRTC Leak", "https://browserleaks.com/webrtc", "🔒", "Stealth Check"),
  SpeedDialItem("Webcam Test", "https://webcamtests.com", "📷", "Virtual Cam"),
  SpeedDialItem("DuckDuckGo", "https://duckduckgo.com", "🦆", "Search"),
  SpeedDialItem("Google", "https://google.com", "🔍", "Search")
)

data class BrowserBookmark(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val url: String,
  val addedAt: Long = System.currentTimeMillis()
)

data class HistoryItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val url: String,
  val visitedAt: Long = System.currentTimeMillis()
)
