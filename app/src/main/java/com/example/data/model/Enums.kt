package com.example.data.model

enum class OSPlatform(val displayName: String, val iconName: String) {
  ANDROID("Android", "android"),
  WINDOWS("Windows", "laptop"),
  MACOS("macOS", "desktop_mac"),
  LINUX("Linux", "terminal"),
  IOS("iOS", "phone_iphone")
}

enum class ProxyProtocol(val label: String, val scheme: String, val defaultPort: Int) {
  HTTP("HTTP", "http", 8080),
  HTTPS("HTTPS", "https", 8443),
  SOCKS4("SOCKS4", "socks", 1080),
  SOCKS4A("SOCKS4a", "socks", 1080),
  SOCKS5("SOCKS5", "socks", 1080),
  SHADOWSOCKS("Shadowsocks", "socks", 8388),
  SSH("SSH Tunnel", "socks", 22)
}

enum class WebRtcPolicy(val displayName: String, val description: String) {
  DISABLED("Disabled", "Completely block WebRTC to eliminate IP leak risk"),
  PROXY_IP("Proxy IP Only", "Force WebRTC traffic through active proxy address"),
  FAKE_IP("Fake IP", "Spoof local and public ICE candidate IP addresses"),
  REAL("Real (Default)", "Standard browser WebRTC behavior")
}

enum class DnsPolicy(val displayName: String, val server: String) {
  BASED_ON_IP("Based on IP", "Match proxy upstream DNS"),
  CLOUDFLARE("Cloudflare", "1.1.1.1"),
  GOOGLE("Google Public DNS", "8.8.8.8"),
  QUAD9("Quad9 DNS", "9.9.9.9")
}

enum class GeolocationPolicy(val displayName: String) {
  BASED_ON_IP("Based on Proxy IP"),
  CUSTOM("Custom Coordinates"),
  ALLOW("Always Allow"),
  BLOCK("Always Block"),
  PROMPT("Prompt User")
}

enum class ProxyType(val displayName: String) {
  RESIDENTIAL("Residential"),
  DATACENTER("Datacenter"),
  MOBILE("Mobile 4G/5G"),
  DEDICATED("Dedicated / Static"),
  ROTATING_GATEWAY("Rotating Gateway / Sticky")
}

enum class VirtualCameraMediaType(val displayName: String) {
  SYNTHETIC("Synthetic Graphic Generator"),
  IMAGE("Custom Image Feed"),
  VIDEO("Custom Video Feed")
}
