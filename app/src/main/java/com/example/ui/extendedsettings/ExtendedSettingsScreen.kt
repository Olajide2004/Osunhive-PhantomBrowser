package com.example.ui.extendedsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.local.entity.ProfileEntity
import com.example.data.model.DnsPolicy
import com.example.data.model.GeolocationPolicy
import com.example.data.model.WebRtcPolicy
import com.example.ui.common.OsIconBadge
import com.example.ui.common.RiskScoreBadge
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendedSettingsScreen(
  viewModel: ExtendedSettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val profile = uiState.profile ?: return

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = profile.name.ifEmpty { "New Profile" },
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Text(
              text = "Extended Fingerprint & Sandbox Settings",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("extended_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Button(
            onClick = { viewModel.saveProfile(onNavigateBack) },
            colors = ButtonDefaults.buttonColors(
              containerColor = PhantomGreen,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("extended_save_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save", fontWeight = FontWeight.Bold)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Section Tab Selector
      ScrollableTabRow(
        selectedTabIndex = uiState.activeSection.ordinal,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PhantomGreen
      ) {
        ExtendedSection.values().forEach { section ->
          Tab(
            selected = uiState.activeSection == section,
            onClick = { viewModel.setSection(section) },
            text = { Text(section.title, fontWeight = if (uiState.activeSection == section) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("tab_${section.name}")
          )
        }
      }

      // Content for the active section
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        when (uiState.activeSection) {
          ExtendedSection.OVERVIEW -> OverviewSectionContent(
            profile = profile,
            uiState = uiState,
            viewModel = viewModel
          )
          ExtendedSection.PROXY_LOCATION -> ProxyLocationSectionContent(
            profile = profile,
            uiState = uiState,
            viewModel = viewModel
          )
          ExtendedSection.BROWSER -> BrowserSectionContent(
            profile = profile,
            viewModel = viewModel
          )
          ExtendedSection.STORAGE -> StorageSectionContent(
            profile = profile,
            viewModel = viewModel
          )
          ExtendedSection.HARDWARE -> HardwareSectionContent(
            profile = profile,
            viewModel = viewModel
          )
        }
      }

      // Bottom Consistency Status Bar
      uiState.consistencyReport?.let { report ->
        ConsistencyBottomBar(report = report)
      }
    }
  }
}

@Composable
fun OverviewSectionContent(
  profile: ProfileEntity,
  uiState: ExtendedSettingsUiState,
  viewModel: ExtendedSettingsViewModel
) {
  val osOptions = listOf("Android", "Windows", "macOS", "Linux", "iOS")

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      // Fingerprint Summary Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PhantomGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Active Fingerprint", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            uiState.consistencyReport?.let {
              RiskScoreBadge(riskScore = it.riskScore, riskLabel = it.riskLabel)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = profile.fingerprintSummary,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "User-Agent: ${profile.userAgent}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = { viewModel.generateNewFingerprint() },
            colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen.copy(alpha = 0.15f), contentColor = PhantomGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("generate_new_fingerprint_button")
          ) {
            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate New Random Fingerprint", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = profile.name,
        onValueChange = { viewModel.updateProfile { p -> p.copy(name = it) } },
        label = { Text("Profile Name") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("overview_name_input")
      )
    }

    item {
      OutlinedTextField(
        value = profile.note,
        onValueChange = { viewModel.updateProfile { p -> p.copy(note = it) } },
        label = { Text("Notes / Purpose") },
        placeholder = { Text("e.g. Account credentials, campaign target") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Text(
        text = "Operating System",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        osOptions.forEach { os ->
          FilterChip(
            selected = profile.os == os,
            onClick = {
              val newUa = FingerprintGenerator.generateUserAgent(os)
              val fp = FingerprintGenerator.generateConsistentFingerprint(os, profile.proxyCountry)
              viewModel.updateProfile { p ->
                p.copy(
                  os = os,
                  userAgent = newUa,
                  resolutionWidth = fp["resW"] as Int,
                  resolutionHeight = fp["resH"] as Int,
                  deviceMemoryGb = fp["memory"] as Int,
                  cpuThreads = fp["threads"] as Int,
                  webGlVendor = fp["gpuVendor"] as String,
                  webGlRenderer = fp["gpuRenderer"] as String,
                  fingerprintSummary = "Switched to $os stealth profile"
                )
              }
            },
            label = { Text(os) },
            leadingIcon = { OsIconBadge(os = os) },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
              selectedLabelColor = PhantomGreen
            )
          )
        }
      }
    }
  }
}

@Composable
fun ProxyLocationSectionContent(
  profile: ProfileEntity,
  uiState: ExtendedSettingsUiState,
  viewModel: ExtendedSettingsViewModel
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      Text(
        text = "Select Active Proxy",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        FilterChip(
          selected = profile.proxyId == null,
          onClick = {
            viewModel.updateProfile { p ->
              p.copy(proxyId = null, proxyCountry = "DIRECT")
            }
          },
          label = { Text("🌐 Direct (No Proxy)") },
          shape = RoundedCornerShape(10.dp)
        )
        uiState.proxies.forEach { proxy ->
          FilterChip(
            selected = profile.proxyId == proxy.id,
            onClick = {
              val (autoTz, autoCoords) = FingerprintGenerator.getTimezoneForCountry(proxy.countryCode)
              val autoLang = FingerprintGenerator.getLanguagesForCountry(proxy.countryCode)
              viewModel.updateProfile { p ->
                p.copy(
                  proxyId = proxy.id,
                  proxyCountry = proxy.countryCode,
                  timezone = autoTz,
                  latitude = autoCoords.first,
                  longitude = autoCoords.second,
                  languages = autoLang
                )
              }
            },
            label = { Text("${getCountryFlagEmoji(proxy.countryCode)} ${proxy.name}") },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }
    }

    item {
      OutlinedTextField(
        value = profile.timezone,
        onValueChange = { viewModel.updateProfile { p -> p.copy(timezone = it) } },
        label = { Text("Timezone") },
        supportingText = { Text("Default 'Based on IP' or e.g. America/New_York, Europe/Berlin") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Text("Geolocation Spoofing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = profile.latitude.toString(),
          onValueChange = {
            it.toDoubleOrNull()?.let { lat ->
              viewModel.updateProfile { p -> p.copy(latitude = lat) }
            }
          },
          label = { Text("Latitude") },
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = profile.longitude.toString(),
          onValueChange = {
            it.toDoubleOrNull()?.let { lon ->
              viewModel.updateProfile { p -> p.copy(longitude = lon) }
            }
          },
          label = { Text("Longitude") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Text("WebRTC IP Leak Protection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))
      val webrtcOptions = listOf("Disabled", "Proxy IP Only", "Fake IP", "Real")
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        webrtcOptions.forEach { opt ->
          FilterChip(
            selected = profile.webrtcPolicy == opt,
            onClick = { viewModel.updateProfile { p -> p.copy(webrtcPolicy = opt) } },
            label = { Text(opt) },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }
    }

    item {
      Text("DNS Policy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))
      val dnsOptions = listOf("Based on IP", "Cloudflare", "Google Public DNS", "Quad9")
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        dnsOptions.forEach { opt ->
          FilterChip(
            selected = profile.dnsPolicy == opt,
            onClick = { viewModel.updateProfile { p -> p.copy(dnsPolicy = opt) } },
            label = { Text(opt) },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }
    }
  }
}

@Composable
fun BrowserSectionContent(
  profile: ProfileEntity,
  viewModel: ExtendedSettingsViewModel
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      OutlinedTextField(
        value = profile.userAgent,
        onValueChange = { viewModel.updateProfile { p -> p.copy(userAgent = it) } },
        label = { Text("User-Agent String") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = profile.startUrl,
        onValueChange = { viewModel.updateProfile { p -> p.copy(startUrl = it) } },
        label = { Text("Start URL") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = profile.languages,
        onValueChange = { viewModel.updateProfile { p -> p.copy(languages = it) } },
        label = { Text("Languages Header (Accept-Language)") },
        supportingText = { Text("e.g. en-US,en;q=0.9,de-DE;q=0.8") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = profile.launchArguments,
        onValueChange = { viewModel.updateProfile { p -> p.copy(launchArguments = it) } },
        label = { Text("Launch Arguments") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
fun StorageSectionContent(
  profile: ProfileEntity,
  viewModel: ExtendedSettingsViewModel
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      Text(
        text = "Isolated Profile Storage Sandbox",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
      Text(
        text = "Every profile session runs with separate cookie databases, cache partitions, and IndexedDB stores.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(10.dp))
    }

    item {
      StorageToggleRow(
        title = "Save Browsing History",
        description = "Retain visited URLs and page navigation between launches",
        checked = profile.saveHistory,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(saveHistory = it) } }
      )
    }

    item {
      StorageToggleRow(
        title = "Save Bookmarks",
        description = "Store saved pages inside profile sandbox",
        checked = profile.saveBookmarks,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(saveBookmarks = it) } }
      )
    }

    item {
      StorageToggleRow(
        title = "Save Passwords & Form Autofill",
        description = "Store isolated form logins and credentials",
        checked = profile.savePasswords,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(savePasswords = it) } }
      )
    }

    item {
      StorageToggleRow(
        title = "Local Storage & IndexedDB",
        description = "Allow websites to store persistent token data per session",
        checked = profile.saveLocalStorage,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(saveLocalStorage = it) } }
      )
    }

    item {
      StorageToggleRow(
        title = "Web Extensions Sandbox",
        description = "Enable isolated user scripts and content modifiers",
        checked = profile.enableExtensions,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(enableExtensions = it) } }
      )
    }
  }
}

@Composable
fun StorageToggleRow(
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(14.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = PhantomGreen
        )
      )
    }
  }
}

@Composable
fun HardwareSectionContent(
  profile: ProfileEntity,
  viewModel: ExtendedSettingsViewModel
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      Text("Screen Resolution", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = profile.resolutionWidth.toString(),
          onValueChange = {
            it.toIntOrNull()?.let { w -> viewModel.updateProfile { p -> p.copy(resolutionWidth = w) } }
          },
          label = { Text("Width (px)") },
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = profile.resolutionHeight.toString(),
          onValueChange = {
            it.toIntOrNull()?.let { h -> viewModel.updateProfile { p -> p.copy(resolutionHeight = h) } }
          },
          label = { Text("Height (px)") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Text("Hardware Specs (Concurrency & RAM)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = profile.cpuThreads.toString(),
          onValueChange = {
            it.toIntOrNull()?.let { c -> viewModel.updateProfile { p -> p.copy(cpuThreads = c) } }
          },
          label = { Text("CPU Threads") },
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = profile.deviceMemoryGb.toString(),
          onValueChange = {
            it.toIntOrNull()?.let { m -> viewModel.updateProfile { p -> p.copy(deviceMemoryGb = m) } }
          },
          label = { Text("RAM (GB)") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      StorageToggleRow(
        title = "Canvas Noise Injection",
        description = "Adds imperceptible 1-bit pseudo-random pixel noise to foil canvas tracking",
        checked = profile.canvasNoiseEnabled,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(canvasNoiseEnabled = it) } }
      )
    }

    item {
      StorageToggleRow(
        title = "AudioContext Noise Injection",
        description = "Alters Web Audio buffer frequencies slightly to produce unique audio fingerprint",
        checked = profile.audioNoiseEnabled,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(audioNoiseEnabled = it) } }
      )
    }

    item {
      Text("WebGL Vendor & Renderer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = profile.webGlVendor,
        onValueChange = { viewModel.updateProfile { p -> p.copy(webGlVendor = it) } },
        label = { Text("WebGL Vendor") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = profile.webGlRenderer,
        onValueChange = { viewModel.updateProfile { p -> p.copy(webGlRenderer = it) } },
        label = { Text("WebGL Renderer") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      Text("Virtual Camera & Media Devices", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      Text(
        "Emulate a real hardware webcam for WebRTC and camera tests without exposing device sensors.",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(10.dp))
      StorageToggleRow(
        title = "Virtual Camera Feed",
        description = "Provides animated synthetic MediaStream for getUserMedia camera requests",
        checked = profile.virtualCameraEnabled,
        onCheckedChange = { viewModel.updateProfile { p -> p.copy(virtualCameraEnabled = it) } }
      )
    }

    if (profile.virtualCameraEnabled) {
      item {
        OutlinedTextField(
          value = profile.virtualCameraName,
          onValueChange = { viewModel.updateProfile { p -> p.copy(virtualCameraName = it) } },
          label = { Text("Virtual Camera Device Label") },
          supportingText = { Text("e.g. Logitech HD Pro Webcam C920, FaceTime HD Camera") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }

      item {
        Text("Virtual Feed Simulation Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        val feedModes = listOf("Color Bars & Timer", "Matrix Digital Wave", "Cyber Avatar")
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
          feedModes.forEach { mode ->
            FilterChip(
              selected = profile.virtualCameraMode == mode,
              onClick = { viewModel.updateProfile { p -> p.copy(virtualCameraMode = mode) } },
              label = { Text(mode) },
              shape = RoundedCornerShape(10.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                selectedLabelColor = PhantomGreen
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun ConsistencyBottomBar(report: com.example.data.fingerprint.ConsistencyReport) {
  Surface(
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Entropy & Consistency Analysis", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        RiskScoreBadge(riskScore = report.riskScore, riskLabel = report.riskLabel)
      }
      if (report.warnings.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "⚠️ ${report.warnings.first()}",
          fontSize = 11.sp,
          color = StatusWarning,
          maxLines = 1
        )
      }
    }
  }
}
