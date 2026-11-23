package com.example.data.proxy

import android.content.Context
import android.util.Log
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.data.local.entity.ProxyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

data class ProxyCheckResult(
  val success: Boolean,
  val latencyMs: Long,
  val detectedIp: String = "",
  val detectedCountry: String = "",
  val errorMessage: String? = null
)

object ProxyTester {
  private const val TAG = "ProxyTester"

  suspend fun testProxy(
    protocol: String,
    host: String,
    port: Int,
    username: String = "",
    password: String = ""
  ): ProxyCheckResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val proxyType = when (protocol.uppercase()) {
        "SOCKS5", "SOCKS" -> Proxy.Type.SOCKS
        else -> Proxy.Type.HTTP
      }

      val javaProxy = Proxy(proxyType, InetSocketAddress(host, port))

      val builder = OkHttpClient.Builder()
        .proxy(javaProxy)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)

      if (username.isNotEmpty()) {
        builder.proxyAuthenticator(object : Authenticator {
          override fun authenticate(route: Route?, response: Response): Request? {
            val credential = Credentials.basic(username, password)
            return response.request.newBuilder()
              .header("Proxy-Authorization", credential)
              .build()
          }
        })
      }

      val client = builder.build()
      val request = Request.Builder()
        .url("https://api.ipify.org?format=json")
        .build()

      val response = client.newCall(request).execute()
      val latency = System.currentTimeMillis() - startTime

      if (response.isSuccessful) {
        val body = response.body?.string() ?: "{}"
        val json = JSONObject(body)
        val ip = json.optString("ip", "Unknown")
        ProxyCheckResult(
          success = true,
          latencyMs = latency,
          detectedIp = ip,
          detectedCountry = "Verified"
        )
      } else {
        ProxyCheckResult(
          success = false,
          latencyMs = latency,
          errorMessage = "HTTP ${response.code}: ${response.message}"
        )
      }
    } catch (e: Exception) {
      val latency = System.currentTimeMillis() - startTime
      Log.w(TAG, "Proxy test failed: ${e.message}")
      ProxyCheckResult(
        success = false,
        latencyMs = latency,
        errorMessage = e.message ?: "Connection timeout or proxy unreachable"
      )
    }
  }
}

object ProxyControllerManager {
  private const val TAG = "ProxyControllerManager"

  fun applyProxy(proxy: ProxyEntity?, onComplete: () -> Unit = {}) {
    if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
      Log.w(TAG, "PROXY_OVERRIDE feature is not supported on this device/WebView")
      onComplete()
      return
    }

    val controller = ProxyController.getInstance()
    val executor = Dispatchers.Main.asExecutor()

    if (proxy == null || proxy.host.isBlank() || proxy.host.endsWith("phantomproxy.net")) {
      controller.clearProxyOverride(executor, Runnable {
        Log.d(TAG, "Proxy override cleared (Direct or Simulated Node)")
        onComplete()
      })
      return
    }

    try {
      val scheme = when (proxy.protocol.uppercase()) {
        "SOCKS5", "SOCKS" -> "socks"
        "HTTPS" -> "https"
        else -> "http"
      }
      val proxyUrl = "$scheme://${proxy.host}:${proxy.port}"

      val proxyConfig = ProxyConfig.Builder()
        .addProxyRule(proxyUrl)
        .addDirect()
        .build()

      controller.setProxyOverride(proxyConfig, executor, Runnable {
        Log.d(TAG, "Proxy override applied: $proxyUrl")
        onComplete()
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to apply proxy override", e)
      controller.clearProxyOverride(executor, Runnable { onComplete() })
    }
  }

  fun clearProxy(onComplete: () -> Unit = {}) {
    if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
      onComplete()
      return
    }
    ProxyController.getInstance().clearProxyOverride(Dispatchers.Main.asExecutor(), Runnable {
      onComplete()
    })
  }
}
