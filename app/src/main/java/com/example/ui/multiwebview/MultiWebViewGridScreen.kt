package com.example.ui.multiwebview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.local.entity.ProxyEntity
import com.example.data.proxy.ProxyControllerManager
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import java.util.UUID

data class MultiWebViewTile(
  val id: String,
  var url: String,
  val proxy: ProxyEntity?,
  val userAgent: String,
  var title: String = "Osunhive Portal",
  var isLoading: Boolean = true,
  var hasError: Boolean = false,
  var connectionState: String = "Connecting...",
  var isMuted: Boolean = true,
  var reloadTrigger: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiWebViewGridScreen(
  availableProxies: List<ProxyEntity>,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  // Grid count: 4, 6, 8, 9, 10, 12
  var gridCount by remember { mutableIntStateOf(4) }
  var autoRefreshEnabled by remember { mutableStateOf(false) }
  var refreshIntervalSeconds by remember { mutableIntStateOf(60) } // 1 minute default
  var globalUrl by remember { mutableStateOf("https://www.osunhive.name.ng") }
  
  var fullscreenTileId by remember { mutableStateOf<String?>(null) }
  var showCookieDialogForTile by remember { mutableStateOf<MultiWebViewTile?>(null) }
  var cookieInputText by remember { mutableStateOf("") }
  var cookieOutputText by remember { mutableStateOf("") }

  // Generate tiles based on gridCount with https://www.osunhive.name.ng
  val tiles = remember(gridCount) {
    mutableStateListOf<MultiWebViewTile>().apply {
      for (i in 0 until gridCount) {
        val randomProxy = availableProxies.randomOrNull()
        val osOptions = listOf("Windows", "macOS", "Linux", "Android", "iOS")
        val randomOs = osOptions.random()
        val ua = FingerprintGenerator.generateUserAgent(randomOs)
        add(
          MultiWebViewTile(
            id = UUID.randomUUID().toString(),
            url = "https://www.osunhive.name.ng",
            proxy = randomProxy,
            userAgent = ua
          )
        )
      }
    }
  }

  // Auto refresh 1 minute effect
  LaunchedEffect(autoRefreshEnabled, refreshIntervalSeconds) {
    if (autoRefreshEnabled) {
      while (true) {
        delay(refreshIntervalSeconds * 1000L)
        tiles.forEach { tile ->
          tile.reloadTrigger++
        }
      }
    }
  }

  fun randomizeAllProxiesAndAgents() {
    tiles.forEach { tile ->
      val randomProxy = availableProxies.randomOrNull()
      val randomOs = listOf("Windows", "macOS", "Linux", "Android", "iOS").random()
      val ua = FingerprintGenerator.generateUserAgent(randomOs)
      val idx = tiles.indexOf(tile)
      if (idx >= 0) {
        tiles[idx] = tile.copy(
          proxy = randomProxy,
          userAgent = ua,
          connectionState = "Rotated Proxy",
          reloadTrigger = tile.reloadTrigger + 1
        )
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Multi-WebView Grid (${tiles.size} Active)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
              "URL: www.osunhive.name.ng • Auto Refresh: ${if (autoRefreshEnabled) "${refreshIntervalSeconds}s" else "Off"}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          IconButton(
            onClick = { randomizeAllProxiesAndAgents() },
            modifier = Modifier.testTag("shuffle_proxies_agents_button")
          ) {
            Icon(Icons.Default.Shuffle, contentDescription = "Randomize Proxies & Agents", tint = PhantomGreen)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      // Blogspot RSS Notification Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PhantomGreen.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Icon(Icons.Default.RssFeed, contentDescription = null, tint = PhantomGreen, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("Blogspot RSS Notification", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PhantomGreen)
            Text("Feed Live: https://www.osunhive.name.ng (Auto-syncing updates)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Control Bar: Grid Count selector & Auto Refresh toggle
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = PhantomGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Auto Refresh (1 min)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Switch(
              checked = autoRefreshEnabled,
              onCheckedChange = { autoRefreshEnabled = it },
              modifier = Modifier.testTag("auto_refresh_switch")
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Grid size selector chips (4, 6, 8, 9, 10, 12)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            Text("Grid Count:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 6.dp))
            val counts = listOf(4, 6, 8, 9, 10, 12)
            counts.forEach { count ->
              FilterChip(
                selected = gridCount == count,
                onClick = {
                  gridCount = count
                  tiles.clear()
                  for (i in 0 until count) {
                    val randomProxy = availableProxies.randomOrNull()
                    val randomOs = listOf("Windows", "macOS", "Linux", "Android", "iOS").random()
                    val ua = FingerprintGenerator.generateUserAgent(randomOs)
                    tiles.add(
                      MultiWebViewTile(
                        id = UUID.randomUUID().toString(),
                        url = globalUrl,
                        proxy = randomProxy,
                        userAgent = ua
                      )
                    )
                  }
                },
                label = { Text("$count Views") },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                modifier = Modifier
                  .padding(end = 4.dp)
                  .testTag("grid_count_$count")
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Global URL Input
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = globalUrl,
              onValueChange = { globalUrl = it },
              placeholder = { Text("https://www.osunhive.name.ng") },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("global_url_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                tiles.forEach { tile ->
                  tile.url = globalUrl
                  tile.reloadTrigger++
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("apply_global_url_button")
            ) {
              Text("Apply All", fontSize = 12.sp, color = Color.White)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Responsive GridView Layout for Multiple Webview Instances
      val columns = when {
        gridCount <= 4 -> 2
        gridCount <= 9 -> 3
        else -> 4
      }

      LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(tiles, key = { it.id }) { tile ->
          MultiWebViewTileCard(
            tile = tile,
            onReload = { tile.reloadTrigger++ },
            onToggleMute = { tile.isMuted = !tile.isMuted },
            onOpenCookies = {
              showCookieDialogForTile = tile
              val cm = CookieManager.getInstance()
              cookieOutputText = cm.getCookie(tile.url) ?: "No cookies found"
              cookieInputText = ""
            },
            onFullscreen = {
              fullscreenTileId = if (fullscreenTileId == tile.id) null else tile.id
            },
            isFullscreen = fullscreenTileId == tile.id
          )
        }
      }
    }
  }

  // Cookie Manager Dialog for individual tile
  if (showCookieDialogForTile != null) {
    val tile = showCookieDialogForTile!!
    AlertDialog(
      onDismissRequest = { showCookieDialogForTile = null },
      title = { Text("Cookie Manager & Import/Export") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Target URL: ${tile.url}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text("Current Session Cookies (Exported):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          OutlinedTextField(
            value = cookieOutputText,
            onValueChange = {},
            readOnly = true,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedButton(
            onClick = { clipboardManager.setText(AnnotatedString(cookieOutputText)) },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Cookie, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy Cookies to Clipboard")
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("Import Cookie String:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          OutlinedTextField(
            value = cookieInputText,
            onValueChange = { cookieInputText = it },
            placeholder = { Text("name=value; domain=.osunhive.name.ng") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (cookieInputText.isNotBlank()) {
              val cm = CookieManager.getInstance()
              cm.setAcceptCookie(true)
              cm.setCookie(tile.url, cookieInputText)
              cm.flush()
            }
            showCookieDialogForTile = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen)
        ) {
          Text("Inject / Import", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCookieDialogForTile = null }) {
          Text("Close")
        }
      }
    )
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MultiWebViewTileCard(
  tile: MultiWebViewTile,
  onReload: () -> Unit,
  onToggleMute: () -> Unit,
  onOpenCookies: () -> Unit,
  onFullscreen: () -> Unit,
  isFullscreen: Boolean
) {
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    modifier = Modifier
      .fillMaxWidth()
      .height(if (isFullscreen) 520.dp else 270.dp)
      .testTag("webview_tile_${tile.id}")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Tile Header Toolbar with Individual Status Indicator
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Individual Status Indicator dot and label
            val (statusColor, statusText) = when {
              tile.hasError -> Pair(StatusError, "Error")
              tile.isLoading -> Pair(StatusWarning, "Loading")
              else -> Pair(StatusActive, "Connected")
            }
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            val proxyLabel = tile.proxy?.let { "${getCountryFlagEmoji(it.countryCode)} ${it.protocol}" } ?: "🌐 Direct"
            Text(
              text = "$proxyLabel • $statusText",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenCookies, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Cookie, contentDescription = "Cookies", modifier = Modifier.size(13.dp), tint = PhantomGreen)
            }
            IconButton(onClick = onReload, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(13.dp))
            }
            IconButton(onClick = onFullscreen, modifier = Modifier.size(24.dp)) {
              Icon(
                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Fullscreen",
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }

      // WebView Body
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        androidx.compose.runtime.key(tile.id, tile.reloadTrigger, tile.proxy?.id) {
          AndroidView(
            factory = { ctx ->
              WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                  javaScriptEnabled = true
                  domStorageEnabled = true
                  databaseEnabled = true
                  userAgentString = tile.userAgent
                  useWideViewPort = true
                  loadWithOverviewMode = true
                  mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                // Apply proxy if present
                if (tile.proxy != null) {
                  ProxyControllerManager.applyProxy(tile.proxy)
                }

                webViewClient = object : WebViewClient() {
                  override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    tile.isLoading = true
                    tile.hasError = false
                    tile.connectionState = "Loading"
                    url?.let { tile.url = it }
                  }

                  override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    tile.isLoading = false
                    tile.hasError = false
                    tile.connectionState = "Connected"
                    url?.let { tile.url = it }
                    view?.title?.let { if (it.isNotBlank()) tile.title = it }
                  }

                  override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                      tile.isLoading = false
                      tile.hasError = true
                      tile.connectionState = "Error"
                    }
                  }
                }

                loadUrl(tile.url)
                webViewRef = this
              }
            },
            update = { webView ->
              webViewRef = webView
              if (webView.url != tile.url) {
                webView.loadUrl(tile.url)
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}
