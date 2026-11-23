package com.example.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.fingerprint.FingerprintScriptInjector
import com.example.data.local.entity.ProfileEntity
import com.example.data.proxy.ProxyControllerManager
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusWarning
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserSessionScreen(
  profile: ProfileEntity,
  onCloseSession: (dataUsedMb: Double) -> Unit,
  modifier: Modifier = Modifier
) {
  // Tabs management
  val tabs = remember {
    mutableStateListOf(
      BrowserTab(
        id = UUID.randomUUID().toString(),
        url = profile.startUrl.ifEmpty { "chrome://newtab" },
        title = "Phantom Start"
      )
    )
  }
  var selectedTabId by remember { mutableStateOf(tabs[0].id) }
  val activeTab = tabs.find { it.id == selectedTabId } ?: tabs[0]

  var isTabSwitcherOpen by remember { mutableStateOf(false) }

  // WebView reference
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var urlInputText by remember { mutableStateOf(if (activeTab.url == "chrome://newtab") "" else activeTab.url) }
  var isAddressBarFocused by remember { mutableStateOf(false) }
  var pageLoadError by remember { mutableStateOf<String?>(null) }
  var loadingProgress by remember { mutableFloatStateOf(0f) }
  var isLoading by remember { mutableStateOf(false) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }

  // Modals & Drawers
  var showChromeMenu by remember { mutableStateOf(false) }
  var showFingerprintSheet by remember { mutableStateOf(false) }
  var showHistoryDialog by remember { mutableStateOf(false) }
  var showBookmarksDialog by remember { mutableStateOf(false) }
  var showFindInPageBar by remember { mutableStateOf(false) }
  var findInPageText by remember { mutableStateOf("") }

  // Bookmarks & History
  val bookmarks = remember {
    mutableStateListOf(
      BrowserBookmark(title = "BrowserLeaks IP Test", url = "https://browserleaks.com/ip"),
      BrowserBookmark(title = "AmIUnique Fingerprint", url = "https://amiunique.org/fingerprint"),
      BrowserBookmark(title = "DuckDuckGo", url = "https://duckduckgo.com")
    )
  }
  val history = remember { mutableStateListOf<HistoryItem>() }

  var sessionDataUsedMb by remember { mutableFloatStateOf(0.5f) }
  var isRendererCrashed by remember { mutableStateOf(false) }
  var reloadTrigger by remember { mutableIntStateOf(0) }

  val script = remember(profile) { FingerprintScriptInjector.buildInjectionScript(profile) }

  fun resolveInputUrl(raw: String): String {
    val q = raw.trim()
    if (q.isEmpty()) return "chrome://newtab"
    if (q.equals("chrome://newtab", ignoreCase = true) || q.equals("about:newtab", ignoreCase = true)) {
      return "chrome://newtab"
    }
    if (q.equals("about:blank", ignoreCase = true)) {
      return "about:blank"
    }
    if (q.startsWith("http://", ignoreCase = true) || q.startsWith("https://", ignoreCase = true) || q.startsWith("file://", ignoreCase = true)) {
      return q
    }
    if (q.startsWith("localhost", ignoreCase = true) || q.startsWith("127.0.0.1")) {
      return "http://$q"
    }
    val isDomainLike = q.contains(".") && !q.contains(" ") && !q.endsWith(".")
    return if (isDomainLike) {
      "https://$q"
    } else {
      "https://duckduckgo.com/?q=${Uri.encode(q)}"
    }
  }

  // Sync url input with active tab
  fun navigateActiveTab(url: String) {
    val dest = resolveInputUrl(url)
    activeTab.url = dest
    pageLoadError = null
    urlInputText = if (dest == "chrome://newtab") "" else dest
    isTabSwitcherOpen = false
    if (dest != "chrome://newtab") {
      history.add(0, HistoryItem(title = activeTab.title.ifEmpty { dest }, url = dest))
      webViewRef?.loadUrl(dest)
    }
  }

  // Handle hardware / gesture back button
  BackHandler {
    if (isTabSwitcherOpen) {
      isTabSwitcherOpen = false
    } else if (showFindInPageBar) {
      showFindInPageBar = false
      webViewRef?.clearMatches()
    } else if (webViewRef?.canGoBack() == true && activeTab.url != "chrome://newtab") {
      webViewRef?.goBack()
    } else {
      ProxyControllerManager.clearProxy()
      onCloseSession(sessionDataUsedMb.toDouble())
    }
  }

  DisposableEffect(profile.id) {
    onDispose {
      ProxyControllerManager.clearProxy()
      webViewRef?.destroy()
    }
  }

  // TAB SWITCHER VIEW (Chrome Grid)
  if (isTabSwitcherOpen) {
    TabSwitcherGrid(
      tabs = tabs,
      selectedTabId = selectedTabId,
      onSelectTab = { id ->
        selectedTabId = id
        val tab = tabs.find { it.id == id }
        if (tab != null) {
          urlInputText = if (tab.url == "chrome://newtab") "" else tab.url
        }
        isTabSwitcherOpen = false
      },
      onCloseTab = { id ->
        if (tabs.size > 1) {
          val idx = tabs.indexOfFirst { it.id == id }
          tabs.removeAll { it.id == id }
          if (selectedTabId == id) {
            val newIdx = idx.coerceAtMost(tabs.size - 1)
            selectedTabId = tabs[newIdx].id
            urlInputText = if (tabs[newIdx].url == "chrome://newtab") "" else tabs[newIdx].url
          }
        } else {
          // If closing the only tab, open New Tab Page
          tabs[0].url = "chrome://newtab"
          tabs[0].title = "New Tab"
          urlInputText = ""
          isTabSwitcherOpen = false
        }
      },
      onNewTab = {
        val newTab = BrowserTab(url = "chrome://newtab", title = "New Tab")
        tabs.add(newTab)
        selectedTabId = newTab.id
        urlInputText = ""
        isTabSwitcherOpen = false
      },
      onCloseAllTabs = {
        tabs.clear()
        val defaultTab = BrowserTab(url = "chrome://newtab", title = "New Tab")
        tabs.add(defaultTab)
        selectedTabId = defaultTab.id
        urlInputText = ""
        isTabSwitcherOpen = false
      },
      onDone = { isTabSwitcherOpen = false },
      modifier = modifier
    )
    return
  }

  // MAIN CHROME BROWSER SCREEN
  Scaffold(
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
      ) {
        // Find in Page Toolbar if active
        AnimatedVisibility(visible = showFindInPageBar) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              OutlinedTextField(
                value = findInPageText,
                onValueChange = {
                  findInPageText = it
                  webViewRef?.findAllAsync(it)
                },
                placeholder = { Text("Find in page...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("find_in_page_input")
              )
              IconButton(onClick = { webViewRef?.findNext(false) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous")
              }
              IconButton(onClick = { webViewRef?.findNext(true) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next")
              }
              IconButton(onClick = {
                showFindInPageBar = false
                webViewRef?.clearMatches()
              }) {
                Icon(Icons.Default.Close, contentDescription = "Close Find")
              }
            }
          }
        }

        // Chrome Profile Identity Strip (Subtle)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(StatusActive)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = profile.name,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "• ${getCountryFlagEmoji(profile.proxyCountry)} ${if (profile.proxyCountry == "DIRECT") "Direct" else profile.proxyCountry}",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activeTab.isDesktopMode) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = PhantomGreen.copy(alpha = 0.2f)
              ) {
                Text(
                  "DESKTOP",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = PhantomGreen,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }

          Text(
            text = "%.1f MB used".format(sessionDataUsedMb),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Chrome Omnibox Toolbar
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          // Home button
          IconButton(
            onClick = { navigateActiveTab("chrome://newtab") },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(Icons.Default.Home, contentDescription = "Home (New Tab)", modifier = Modifier.size(20.dp))
          }

          // Chrome Omnibox Pill
          val isQueryDirty = urlInputText.trim() != activeTab.url.trim() && urlInputText.isNotBlank()
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(
              1.dp,
              when {
                pageLoadError != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                isAddressBarFocused -> PhantomGreen.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
              }
            ),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp)
            ) {
              val lockTint = when {
                pageLoadError != null -> MaterialTheme.colorScheme.error
                activeTab.url.startsWith("https://") -> PhantomGreen
                activeTab.url.startsWith("http://") -> StatusWarning
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              }
              val lockIcon = when {
                pageLoadError != null -> Icons.Default.Warning
                else -> Icons.Default.Lock
              }
              Icon(
                imageVector = lockIcon,
                contentDescription = if (pageLoadError != null) "Connection Error" else "SSL Status",
                tint = lockTint,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))

              BasicTextField(
                value = urlInputText,
                onValueChange = {
                  urlInputText = it
                  if (pageLoadError != null) pageLoadError = null
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 13.sp
                ),
                cursorBrush = SolidColor(PhantomGreen),
                keyboardOptions = KeyboardOptions(
                  imeAction = ImeAction.Go,
                  keyboardType = KeyboardType.Uri
                ),
                keyboardActions = KeyboardActions(onGo = {
                  val query = urlInputText.trim()
                  if (query.isNotEmpty()) {
                    navigateActiveTab(query)
                  }
                }),
                modifier = Modifier
                  .weight(1f)
                  .onFocusChanged { isAddressBarFocused = it.isFocused }
                  .onKeyEvent { keyEvent ->
                    if ((keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) && keyEvent.type == KeyEventType.KeyUp) {
                      val query = urlInputText.trim()
                      if (query.isNotEmpty()) {
                        navigateActiveTab(query)
                      }
                      true
                    } else {
                      false
                    }
                  }
                  .testTag("browser_omnibox_input"),
                decorationBox = { innerTextField ->
                  Box(contentAlignment = Alignment.CenterStart) {
                    if (urlInputText.isEmpty()) {
                      Text(
                        "Search or type URL",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                      )
                    }
                    innerTextField()
                  }
                }
              )

              if (urlInputText.isNotEmpty() && (isAddressBarFocused || isQueryDirty)) {
                IconButton(
                  onClick = { urlInputText = "" },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (isQueryDirty) {
                IconButton(
                  onClick = {
                    val query = urlInputText.trim()
                    if (query.isNotEmpty()) {
                      navigateActiveTab(query)
                    }
                  },
                  modifier = Modifier
                    .size(26.dp)
                    .testTag("browser_omnibox_go_button")
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go to URL",
                    modifier = Modifier.size(17.dp),
                    tint = PhantomGreen
                  )
                }
              } else if (isLoading) {
                IconButton(
                  onClick = { webViewRef?.stopLoading() },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    Icons.Default.Close,
                    contentDescription = "Stop",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              } else if (activeTab.url != "chrome://newtab") {
                IconButton(
                  onClick = { webViewRef?.reload() },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reload",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Chrome Tab Counter Box [2]
          Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
            color = Color.Transparent,
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable { isTabSwitcherOpen = true }
              .testTag("chrome_tab_counter_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "${tabs.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Chrome 3-Dot Overflow Menu
          Box {
            IconButton(
              onClick = { showChromeMenu = true },
              modifier = Modifier.testTag("chrome_more_menu_button")
            ) {
              Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
              expanded = showChromeMenu,
              onDismissRequest = { showChromeMenu = false }
            ) {
              // Navigation actions
              Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp, horizontal = 8.dp)
              ) {
                IconButton(
                  onClick = {
                    showChromeMenu = false
                    webViewRef?.goBack()
                  },
                  enabled = canGoBack
                ) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                IconButton(
                  onClick = {
                    showChromeMenu = false
                    webViewRef?.goForward()
                  },
                  enabled = canGoForward
                ) {
                  Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                }
                IconButton(
                  onClick = {
                    showChromeMenu = false
                    val isAlreadyBookmarked = bookmarks.any { it.url == activeTab.url }
                    if (!isAlreadyBookmarked && activeTab.url != "chrome://newtab") {
                      bookmarks.add(BrowserBookmark(title = activeTab.title, url = activeTab.url))
                    }
                  }
                ) {
                  val isBookmarked = bookmarks.any { it.url == activeTab.url }
                  Icon(
                    if (isBookmarked) Icons.Default.Star else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) PhantomGreen else MaterialTheme.colorScheme.onSurface
                  )
                }
                IconButton(
                  onClick = {
                    showChromeMenu = false
                    webViewRef?.reload()
                  }
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
              }

              HorizontalDivider()

              DropdownMenuItem(
                text = { Text("New tab") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                  showChromeMenu = false
                  val newTab = BrowserTab(url = "chrome://newtab", title = "New Tab")
                  tabs.add(newTab)
                  selectedTabId = newTab.id
                  urlInputText = ""
                }
              )

              DropdownMenuItem(
                text = { Text("Bookmarks (${bookmarks.size})") },
                leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },
                onClick = {
                  showChromeMenu = false
                  showBookmarksDialog = true
                }
              )

              DropdownMenuItem(
                text = { Text("History") },
                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                onClick = {
                  showChromeMenu = false
                  showHistoryDialog = true
                }
              )

              DropdownMenuItem(
                text = { Text("Find in page") },
                leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
                onClick = {
                  showChromeMenu = false
                  showFindInPageBar = true
                }
              )

              DropdownMenuItem(
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Desktop site")
                    if (activeTab.isDesktopMode) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = PhantomGreen)
                    }
                  }
                },
                leadingIcon = {
                  Icon(
                    if (activeTab.isDesktopMode) Icons.Default.DesktopWindows else Icons.Default.Smartphone,
                    contentDescription = null
                  )
                },
                onClick = {
                  showChromeMenu = false
                  activeTab.isDesktopMode = !activeTab.isDesktopMode
                  val newUa = if (activeTab.isDesktopMode) {
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36"
                  } else {
                    profile.userAgent
                  }
                  webViewRef?.settings?.userAgentString = newUa
                  webViewRef?.reload()
                }
              )

              HorizontalDivider()

              DropdownMenuItem(
                text = { Text("Inspect Stealth Fingerprint", color = PhantomGreen, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = PhantomGreen) },
                onClick = {
                  showChromeMenu = false
                  showFingerprintSheet = true
                }
              )

              DropdownMenuItem(
                text = { Text("Stop & Close Session", color = Color.Red) },
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red) },
                onClick = {
                  showChromeMenu = false
                  ProxyControllerManager.clearProxy()
                  onCloseSession(sessionDataUsedMb.toDouble())
                }
              )
            }
          }
        }

        // Progress bar
        if (isLoading) {
          LinearProgressIndicator(
            progress = { loadingProgress },
            color = PhantomGreen,
            trackColor = Color.Transparent,
            modifier = Modifier
              .fillMaxWidth()
              .height(2.5.dp)
          )
        }

        // Page Connection Error Bar
        AnimatedVisibility(visible = pageLoadError != null) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  Icons.Default.Warning,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = pageLoadError ?: "Failed to load page",
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  fontSize = 12.sp,
                  maxLines = 1
                )
              }
              Row {
                TextButton(
                  onClick = {
                    pageLoadError = null
                    webViewRef?.reload()
                  }
                ) {
                  Text("Retry", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                TextButton(
                  onClick = {
                    pageLoadError = null
                    navigateActiveTab("chrome://newtab")
                  }
                ) {
                  Text("New Tab", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (activeTab.url == "chrome://newtab") {
        // CHROME NEW TAB PAGE (NTP)
        NewTabPage(
          profile = profile,
          onNavigateToUrl = { url -> navigateActiveTab(url) }
        )
      } else if (isRendererCrashed) {
        // Crash Recovery Screen
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, StatusWarning),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(44.dp))
              Spacer(modifier = Modifier.height(14.dp))
              Text("Sandbox Render Process Recovered", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "The isolated WebView render process was reset. Tap below to reload the session in safe sandbox mode.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = {
                  isRendererCrashed = false
                  reloadTrigger++
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reload_crashed_session_button")
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reload Session", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        // ACTIVE WEBVIEW
        key(activeTab.id, reloadTrigger) {
          AndroidView(
            factory = { ctx ->
              WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                  javaScriptEnabled = true
                  domStorageEnabled = profile.saveLocalStorage
                  databaseEnabled = profile.saveLocalStorage
                  userAgentString = if (activeTab.isDesktopMode) {
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.114 Safari/537.36"
                  } else {
                    profile.userAgent
                  }
                  useWideViewPort = true
                  loadWithOverviewMode = true
                  setSupportZoom(true)
                  builtInZoomControls = true
                  displayZoomControls = false
                  mediaPlaybackRequiresUserGesture = false
                  mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                  cacheMode = WebSettings.LOAD_DEFAULT
                }

                webViewClient = object : WebViewClient() {
                  override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isLoading = true
                    pageLoadError = null
                    url?.let {
                      activeTab.url = it
                      if (!isAddressBarFocused) {
                        urlInputText = if (it == "chrome://newtab") "" else it
                      }
                    }
                    sessionDataUsedMb += 0.2f
                    try {
                      view?.evaluateJavascript(script, null)
                    } catch (_: Exception) {}
                  }

                  override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading = false
                    canGoBack = view?.canGoBack() ?: false
                    canGoForward = view?.canGoForward() ?: false
                    url?.let {
                      activeTab.url = it
                      if (!isAddressBarFocused) {
                        urlInputText = if (it == "chrome://newtab") "" else it
                      }
                    }
                    try {
                      view?.evaluateJavascript(script, null)
                    } catch (_: Exception) {}
                  }

                  override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                    android.util.Log.e("PhantomWebView", "WebView render process exited: didCrash=${detail?.didCrash()}")
                    try {
                      (view?.parent as? ViewGroup)?.removeView(view)
                      view?.destroy()
                    } catch (_: Exception) {}
                    webViewRef = null
                    isRendererCrashed = true
                    isLoading = false
                    return true
                  }

                  override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                      isLoading = false
                      pageLoadError = error?.description?.toString() ?: "Failed to connect to address"
                    }
                  }

                  override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    isLoading = false
                    if (failingUrl == activeTab.url) {
                      pageLoadError = description ?: "Connection error ($errorCode)"
                    }
                  }

                  override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    pageLoadError = "SSL Certificate Warning"
                    super.onReceivedSslError(view, handler, error)
                  }

                  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                  }
                }

                webChromeClient = object : WebChromeClient() {
                  override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    loadingProgress = newProgress / 100f
                    if (newProgress >= 100) isLoading = false
                  }

                  override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    if (!title.isNullOrBlank()) {
                      activeTab.title = title
                    }
                  }

                  override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                  ) {
                    if (profile.geolocationPolicy == "Always Block") {
                      callback?.invoke(origin, false, false)
                    } else {
                      callback?.invoke(origin, true, false)
                    }
                  }
                }

                loadUrl(activeTab.url)
                webViewRef = this
              }
            },
            update = { webView ->
              webViewRef = webView
              if (activeTab.url != "chrome://newtab" && webView.url != activeTab.url) {
                webView.loadUrl(activeTab.url)
              }
            },
            modifier = Modifier
              .fillMaxSize()
              .testTag("phantom_webview")
          )
        }
      }
    }
  }

  // BOOKMARKS DIALOG
  if (showBookmarksDialog) {
    AlertDialog(
      onDismissRequest = { showBookmarksDialog = false },
      title = { Text("Bookmarks") },
      text = {
        if (bookmarks.isEmpty()) {
          Text("No bookmarks saved.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(bookmarks) { b ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    navigateActiveTab(b.url)
                    showBookmarksDialog = false
                  }
                  .padding(vertical = 10.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(b.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(b.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { bookmarks.remove(b) }) {
                  Icon(Icons.Default.Clear, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showBookmarksDialog = false }) { Text("Close") }
      }
    )
  }

  // HISTORY DIALOG
  if (showHistoryDialog) {
    AlertDialog(
      onDismissRequest = { showHistoryDialog = false },
      title = { Text("Browsing History") },
      text = {
        if (history.isEmpty()) {
          Text("No visited pages in this session yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(history) { h ->
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    navigateActiveTab(h.url)
                    showHistoryDialog = false
                  }
                  .padding(vertical = 8.dp)
              ) {
                Text(h.title.ifEmpty { h.url }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(h.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showHistoryDialog = false }) { Text("Done") }
      }
    )
  }

  // FINGERPRINT INSPECTOR SHEET
  if (showFingerprintSheet) {
    ModalBottomSheet(
      onDismissRequest = { showFingerprintSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
          .padding(bottom = 28.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Stealth Fingerprint Inspector", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = StatusActive.copy(alpha = 0.15f)
          ) {
            Text(
              "INJECTED & ACTIVE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = StatusActive,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Verification Sites
        Text("Test Stealth on Detector Sites", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = {
              navigateActiveTab("https://browserleaks.com/ip")
              showFingerprintSheet = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("BrowserLeaks IP", fontSize = 11.sp)
          }
          Button(
            onClick = {
              navigateActiveTab("https://browserleaks.com/canvas")
              showFingerprintSheet = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Canvas Noise", fontSize = 11.sp)
          }
          Button(
            onClick = {
              navigateActiveTab("https://webcamtests.com")
              showFingerprintSheet = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen.copy(alpha = 0.2f), contentColor = PhantomGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("📷 Cam Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        InspectorRow("Virtual Camera", if (profile.virtualCameraEnabled) "${profile.virtualCameraName} • ${profile.virtualCameraMode}" else "Disabled")
        InspectorRow("Emulated OS", profile.os)
        InspectorRow("Resolution", "${profile.resolutionWidth} x ${profile.resolutionHeight}")
        InspectorRow("CPU Cores / Threads", "${profile.cpuThreads} Cores")
        InspectorRow("Emulated RAM", "${profile.deviceMemoryGb} GB")
        InspectorRow("Canvas Noise", if (profile.canvasNoiseEnabled) "Enabled (1-bit entropy jitter)" else "Disabled")
        InspectorRow("WebGL Vendor", profile.webGlVendor)
        InspectorRow("WebGL Renderer", profile.webGlRenderer)
        InspectorRow("WebRTC Shield", profile.webrtcPolicy)
        InspectorRow("Timezone", profile.timezone)
        InspectorRow("Spoofed Coords", "${profile.latitude}, ${profile.longitude}")
        InspectorRow("Accept-Language", profile.languages)
        InspectorRow("Active User-Agent", if (activeTab.isDesktopMode) "Desktop Chrome" else profile.userAgent)
      }
    }
  }
}

@Composable
fun InspectorRow(label: String, value: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PhantomGreen)
    Text(value, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
  }
}
