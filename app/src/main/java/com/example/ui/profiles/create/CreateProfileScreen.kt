package com.example.ui.profiles.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProfileEntity
import com.example.ui.common.OsIconBadge
import com.example.ui.common.RiskScoreBadge
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProfileScreen(
  viewModel: CreateProfileViewModel,
  onNavigateBack: () -> Unit,
  onProfileCreated: (ProfileEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var newFolderName by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  BackHandler {
    onNavigateBack()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "New Isolated Profile",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Text(
              text = "Per-profile proxy & privacy sandbox",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("create_profile_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Button(
            onClick = {
              viewModel.createProfile { created ->
                onProfileCreated(created)
              }
            },
            enabled = !uiState.isCreating,
            colors = ButtonDefaults.buttonColors(
              containerColor = PhantomGreen,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("create_profile_save_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create", fontWeight = FontWeight.Bold)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          OutlinedButton(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("create_profile_cancel_button")
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              viewModel.createProfile { created ->
                onProfileCreated(created)
              }
            },
            enabled = !uiState.isCreating,
            colors = ButtonDefaults.buttonColors(
              containerColor = PhantomGreen,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(2f)
              .height(48.dp)
              .testTag("create_profile_submit_button")
          ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (uiState.isCreating) "Creating Profile..." else "Create Isolated Profile",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = paddingValues.calculateTopPadding() + 8.dp,
        bottom = paddingValues.calculateBottomPadding() + 16.dp
      ),
      verticalArrangement = Arrangement.spacedBy(18.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Live Stealth & Entropy Status Hero Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          ),
          border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.35f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("stealth_consistency_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = PhantomGreen.copy(alpha = 0.2f),
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Fingerprint,
                      contentDescription = null,
                      tint = PhantomGreen,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Live Fingerprint Entropy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "Hardware noise & proxy consistency",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              uiState.consistencyReport?.let {
                RiskScoreBadge(riskScore = it.riskScore, riskLabel = it.riskLabel)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              StatusPill(
                icon = Icons.Default.Security,
                label = "${uiState.os} Emulation",
                active = true
              )
              StatusPill(
                icon = Icons.Default.Public,
                label = when (uiState.proxyMode) {
                  ProxyMode.DIRECT -> "Direct IP"
                  ProxyMode.SAVED -> "Saved Proxy (${uiState.proxies.find { it.id == uiState.selectedProxyId }?.countryCode ?: "US"})"
                  ProxyMode.CUSTOM -> "Custom Proxy (${uiState.customProxyProtocol})"
                },
                active = uiState.proxyMode != ProxyMode.DIRECT
              )
              StatusPill(
                icon = Icons.Default.Shield,
                label = if (uiState.canvasNoiseEnabled) "Canvas Noise ON" else "Canvas Noise OFF",
                active = uiState.canvasNoiseEnabled
              )
              StatusPill(
                icon = Icons.Default.Lock,
                label = "WebRTC: ${uiState.webrtcPolicy}",
                active = uiState.webrtcPolicy != "Real"
              )
            }

            // Warnings if any
            uiState.consistencyReport?.warnings?.firstOrNull()?.let { warning ->
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .background(StatusWarning.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = StatusWarning,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = warning,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 2
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Resolution: ${uiState.resolutionWidth}x${uiState.resolutionHeight} • ${uiState.deviceMemoryGb}GB RAM",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Surface(
                onClick = { viewModel.regenerateEntropy() },
                shape = RoundedCornerShape(8.dp),
                color = PhantomGreen.copy(alpha = 0.15f),
                modifier = Modifier.testTag("regenerate_entropy_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = null,
                    tint = PhantomGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Regenerate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PhantomGreen
                  )
                }
              }
            }
          }
        }
      }

      // 2. Quick Privacy Presets Bar
      item {
        Column {
          SectionHeader(
            title = "Privacy Presets",
            subtitle = "One-tap isolation configuration for fast profile creation"
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            PresetChip(
              title = "Max Stealth",
              subtitle = "Strict noise & zero logs",
              selected = uiState.privacyPreset == PrivacyPreset.MAXIMUM_STEALTH,
              icon = Icons.Default.Security,
              onClick = { viewModel.applyPrivacyPreset(PrivacyPreset.MAXIMUM_STEALTH) },
              testTag = "preset_max_stealth"
            )
            PresetChip(
              title = "Balanced",
              subtitle = "Masked + bookmarks saved",
              selected = uiState.privacyPreset == PrivacyPreset.BALANCED,
              icon = Icons.Default.Shield,
              onClick = { viewModel.applyPrivacyPreset(PrivacyPreset.BALANCED) },
              testTag = "preset_balanced"
            )
            PresetChip(
              title = "Direct Fast",
              subtitle = "High speed direct connection",
              selected = uiState.privacyPreset == PrivacyPreset.FAST,
              icon = Icons.Default.Speed,
              onClick = { viewModel.applyPrivacyPreset(PrivacyPreset.FAST) },
              testTag = "preset_fast"
            )
            PresetChip(
              title = "Custom",
              subtitle = "Manually fine-tuned",
              selected = uiState.privacyPreset == PrivacyPreset.CUSTOM,
              icon = Icons.Default.Tune,
              onClick = { viewModel.applyPrivacyPreset(PrivacyPreset.CUSTOM) },
              testTag = "preset_custom"
            )
          }
        }
      }

      // 3. Section 1: Profile Identity & Platform
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
              title = "Profile Identity",
              subtitle = "Isolated container name, organization, and OS emulation"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Name Input with Dice
            OutlinedTextField(
              value = uiState.name,
              onValueChange = { viewModel.setName(it) },
              label = { Text("Profile Name") },
              placeholder = { Text("e.g. stealth-matrix, crypto-trader") },
              trailingIcon = {
                IconButton(
                  onClick = { viewModel.generateRandomName() },
                  modifier = Modifier.testTag("randomize_name_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Generate Stealth Name",
                    tint = PhantomGreen
                  )
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhantomGreen,
                cursorColor = PhantomGreen
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes / Purpose Input
            OutlinedTextField(
              value = uiState.note,
              onValueChange = { viewModel.setNote(it) },
              label = { Text("Notes & Isolation Purpose (Optional)") },
              placeholder = { Text("e.g. PayPal sandbox, multi-account ads, QA testing") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhantomGreen,
                cursorColor = PhantomGreen
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_note_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OS Emulation Chips
            Text(
              text = "Operating System Emulation",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            val osList = listOf("Android", "Windows", "macOS", "Linux", "iOS")
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              osList.forEach { os ->
                FilterChip(
                  selected = uiState.os == os,
                  onClick = { viewModel.setOs(os) },
                  label = { Text(os) },
                  leadingIcon = { OsIconBadge(os = os) },
                  shape = RoundedCornerShape(10.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                    selectedLabelColor = PhantomGreen
                  ),
                  border = BorderStroke(
                    1.dp,
                    if (uiState.os == os) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier.testTag("os_chip_$os")
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workspace Folder
            Text(
              text = "Workspace Folder",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
              FilterChip(
                selected = uiState.folderId == null,
                onClick = { viewModel.setFolder(null) },
                label = { Text("None") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                border = BorderStroke(
                  1.dp,
                  if (uiState.folderId == null) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("folder_chip_none")
              )

              uiState.folders.forEach { folder ->
                FilterChip(
                  selected = uiState.folderId == folder.id,
                  onClick = { viewModel.setFolder(folder.id) },
                  label = { Text(folder.name) },
                  shape = RoundedCornerShape(10.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                    selectedLabelColor = PhantomGreen
                  ),
                  border = BorderStroke(
                    1.dp,
                    if (uiState.folderId == folder.id) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier.testTag("folder_chip_${folder.id}")
                )
              }

              Surface(
                onClick = { showNewFolderDialog = true },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.testTag("add_folder_chip")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = PhantomGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("+ Folder", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PhantomGreen)
                }
              }
            }
          }
        }
      }

      // 4. Section 2: Individual Proxy Configuration
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
              title = "Proxy & Network Routing",
              subtitle = "Configure an isolated IP exit node for this profile"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Proxy Mode Selector
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              FilterChip(
                selected = uiState.proxyMode == ProxyMode.DIRECT,
                onClick = { viewModel.setProxyMode(ProxyMode.DIRECT) },
                label = { Text("Direct Connection") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                border = BorderStroke(
                  1.dp,
                  if (uiState.proxyMode == ProxyMode.DIRECT) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("proxy_mode_direct")
              )

              FilterChip(
                selected = uiState.proxyMode == ProxyMode.SAVED,
                onClick = { viewModel.setProxyMode(ProxyMode.SAVED) },
                label = { Text("Saved Pool (${uiState.proxies.size})") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                border = BorderStroke(
                  1.dp,
                  if (uiState.proxyMode == ProxyMode.SAVED) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("proxy_mode_saved")
              )

              FilterChip(
                selected = uiState.proxyMode == ProxyMode.CUSTOM,
                onClick = { viewModel.setProxyMode(ProxyMode.CUSTOM) },
                label = { Text("Custom Node") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                border = BorderStroke(
                  1.dp,
                  if (uiState.proxyMode == ProxyMode.CUSTOM) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("proxy_mode_custom")
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Details
            when (uiState.proxyMode) {
              ProxyMode.DIRECT -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Public,
                      contentDescription = null,
                      tint = PhantomGreen,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = "Direct Network Tunnel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                      )
                      Text(
                        text = "Web traffic routes via current WiFi/Cellular. Browser fingerprint & sandboxing remain strictly isolated.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }

              ProxyMode.SAVED -> {
                if (uiState.proxies.isEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.padding(16.dp)
                    ) {
                      Text("No Saved Proxies in Pool", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        "Switch to 'Custom Node' to configure a proxy directly for this profile.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                } else {
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.proxies.forEach { proxy ->
                      val isSelected = uiState.selectedProxyId == proxy.id
                      Surface(
                        onClick = { viewModel.selectSavedProxy(proxy.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PhantomGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, if (isSelected) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                          .fillMaxWidth()
                          .testTag("saved_proxy_item_${proxy.id}")
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween,
                          modifier = Modifier.padding(12.dp)
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(getCountryFlagEmoji(proxy.countryCode), fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                              Text(proxy.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                              Text(
                                "${proxy.protocol} • ${proxy.host}:${proxy.port} • ${proxy.ipPoolType}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                            }
                          }
                          if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = PhantomGreen, modifier = Modifier.size(18.dp))
                          }
                        }
                      }
                    }
                  }
                }
              }

              ProxyMode.CUSTOM -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  // Protocol chips
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HTTP", "HTTPS", "SOCKS5").forEach { proto ->
                      FilterChip(
                        selected = uiState.customProxyProtocol == proto,
                        onClick = { viewModel.setCustomProxyProtocol(proto) },
                        label = { Text(proto) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                          selectedLabelColor = PhantomGreen
                        ),
                        modifier = Modifier.testTag("custom_proto_$proto")
                      )
                    }
                  }

                  // Host and Port row
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                      value = uiState.customProxyHost,
                      onValueChange = { viewModel.setCustomProxyHost(it) },
                      label = { Text("Proxy Host / IP") },
                      placeholder = { Text("192.168.1.100 or proxy.net") },
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .weight(2f)
                        .testTag("custom_proxy_host_input")
                    )

                    OutlinedTextField(
                      value = uiState.customProxyPort,
                      onValueChange = { viewModel.setCustomProxyPort(it) },
                      label = { Text("Port") },
                      placeholder = { Text("8080") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("custom_proxy_port_input")
                    )
                  }

                  // Username and Password row
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                      value = uiState.customProxyUsername,
                      onValueChange = { viewModel.setCustomProxyUsername(it) },
                      label = { Text("Username") },
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("custom_proxy_user_input")
                    )

                    OutlinedTextField(
                      value = uiState.customProxyPassword,
                      onValueChange = { viewModel.setCustomProxyPassword(it) },
                      label = { Text("Password") },
                      singleLine = true,
                      visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                      trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                          Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                          )
                        }
                      },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("custom_proxy_pass_input")
                    )
                  }

                  // Country Code and Save to Pool
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    OutlinedTextField(
                      value = uiState.customProxyCountry,
                      onValueChange = { viewModel.setCustomProxyCountry(it) },
                      label = { Text("Exit Country Code") },
                      placeholder = { Text("US, DE, GB...") },
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .width(160.dp)
                        .testTag("custom_proxy_country_input")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text("Save to Pool", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                      Spacer(modifier = Modifier.width(6.dp))
                      Switch(
                        checked = uiState.saveCustomProxyToPool,
                        onCheckedChange = { viewModel.setSaveCustomProxyToPool(it) },
                        colors = SwitchDefaults.colors(
                          checkedThumbColor = Color.White,
                          checkedTrackColor = PhantomGreen
                        ),
                        modifier = Modifier.testTag("save_proxy_to_pool_toggle")
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Auto Location & Timezone Sync
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Auto-Match Timezone & Geolocation",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
                Text(
                  text = "Prevents location-leak penalties by matching browser coordinates to the proxy exit IP.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Switch(
                checked = uiState.autoMatchLocationToProxy,
                onCheckedChange = { viewModel.setAutoMatchLocation(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = PhantomGreen
                ),
                modifier = Modifier.testTag("proxy_autosync_location_switch")
              )
            }

            AnimatedVisibility(
              visible = !uiState.autoMatchLocationToProxy,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 10.dp)
              ) {
                OutlinedTextField(
                  value = uiState.timezone,
                  onValueChange = { viewModel.setTimezone(it) },
                  label = { Text("Custom Timezone") },
                  placeholder = { Text("e.g. America/New_York, Europe/London") },
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_timezone_input")
                )
              }
            }
          }
        }
      }

      // 5. Section 3: Individual Privacy & Sandbox Protection
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
              title = "Privacy & Fingerprint Shields",
              subtitle = "Per-profile cryptographic noise, WebRTC protection, and sandbox isolation"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fingerprint toggles
            Text(
              text = "Hardware & Canvas Entropy",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = PhantomGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "HTML5 Canvas Noise Injection",
              description = "Injects sub-perceptual cryptographic pixel noise into 2D canvas reads to neutralize cross-site canvas fingerprinting.",
              checked = uiState.canvasNoiseEnabled,
              onCheckedChange = { viewModel.setCanvasNoise(it) },
              testTag = "privacy_canvas_noise_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "WebGL Vendor & GPU Spoofing",
              description = "Masks internal graphics card strings with standard high-stealth hardware profiles (${uiState.webGlVendor}).",
              checked = uiState.webGlSpoofing,
              onCheckedChange = { viewModel.setWebGlSpoofing(it) },
              testTag = "privacy_webgl_spoofing_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "AudioContext Frequency Jitter",
              description = "Adds micro-randomization to Web Audio API buffer frequencies to foil audio oscillator fingerprinting.",
              checked = uiState.audioNoiseEnabled,
              onCheckedChange = { viewModel.setAudioNoise(it) },
              testTag = "privacy_audio_noise_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "Virtual Camera Device Sandbox",
              description = "Emulates an authentic video device (${uiState.virtualCameraName}) for WebRTC requests without exposing actual hardware cameras.",
              checked = uiState.virtualCameraEnabled,
              onCheckedChange = { viewModel.setVirtualCamera(it) },
              testTag = "privacy_virtual_camera_switch"
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // WebRTC & Network Leak Shield
            Text(
              text = "Network & IP Leak Protection",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = PhantomGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("WebRTC IP Candidate Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val webrtcList = listOf("Proxy IP Only", "Disabled", "Fake IP", "Real")
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              webrtcList.forEach { policy ->
                FilterChip(
                  selected = uiState.webrtcPolicy == policy,
                  onClick = { viewModel.setWebRtcPolicy(policy) },
                  label = { Text(policy) },
                  shape = RoundedCornerShape(10.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                    selectedLabelColor = PhantomGreen
                  ),
                  border = BorderStroke(
                    1.dp,
                    if (uiState.webrtcPolicy == policy) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier.testTag("webrtc_chip_$policy")
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("DNS Resolution Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val dnsList = listOf("Based on IP", "Cloudflare", "Google", "Quad9")
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              dnsList.forEach { dns ->
                FilterChip(
                  selected = uiState.dnsPolicy == dns,
                  onClick = { viewModel.setDnsPolicy(dns) },
                  label = { Text(dns) },
                  shape = RoundedCornerShape(10.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                    selectedLabelColor = PhantomGreen
                  ),
                  border = BorderStroke(
                    1.dp,
                    if (uiState.dnsPolicy == dns) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier.testTag("dns_chip_$dns")
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Storage Isolation
            Text(
              text = "Isolated Storage & Sandboxing",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = PhantomGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "Save Browsing History",
              description = "Retain visited URLs and page logs between launches (disabled for maximum privacy).",
              checked = uiState.saveHistory,
              onCheckedChange = { viewModel.setSaveHistory(it) },
              testTag = "privacy_save_history_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "Save Bookmarks in Sandbox",
              description = "Preserve profile-specific bookmarks inside isolated storage partition.",
              checked = uiState.saveBookmarks,
              onCheckedChange = { viewModel.setSaveBookmarks(it) },
              testTag = "privacy_save_bookmarks_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "Save Form Passwords",
              description = "Allow saving credentials in this profile's isolated database.",
              checked = uiState.savePasswords,
              onCheckedChange = { viewModel.setSavePasswords(it) },
              testTag = "privacy_save_passwords_switch"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrivacyToggleItem(
              title = "Persistent LocalStorage & Cookies",
              description = "Maintain cookies and site tokens for seamless login sessions while keeping other profiles isolated.",
              checked = uiState.saveLocalStorage,
              onCheckedChange = { viewModel.setSaveLocalStorage(it) },
              testTag = "privacy_save_localstorage_switch"
            )
          }
        }
      }
    }
  }

  // New Folder Dialog
  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = { Text("New Workspace Folder") },
      text = {
        OutlinedTextField(
          value = newFolderName,
          onValueChange = { newFolderName = it },
          label = { Text("Folder Name") },
          placeholder = { Text("e.g. Finance, Ad Spying, Crypto") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_folder_name_input")
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderName.isNotBlank()) {
              viewModel.createFolder(newFolderName)
              newFolderName = ""
              showNewFolderDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("confirm_create_folder_button")
        ) {
          Text("Create", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
  Column {
    Text(
      text = title,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = subtitle,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun StatusPill(
  icon: ImageVector,
  label: String,
  active: Boolean
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (active) PhantomGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(
      1.dp,
      if (active) PhantomGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (active) PhantomGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (active) PhantomGreen else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun PresetChip(
  title: String,
  subtitle: String,
  selected: Boolean,
  icon: ImageVector,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    color = if (selected) PhantomGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      1.5.dp,
      if (selected) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    ),
    modifier = Modifier
      .width(150.dp)
      .testTag(testTag)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          shape = CircleShape,
          color = if (selected) PhantomGreen else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(26.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
          }
        }
        if (selected) {
          Icon(Icons.Default.Check, contentDescription = null, tint = PhantomGreen, modifier = Modifier.size(16.dp))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(
        subtitle,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun PrivacyToggleItem(
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = description,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = PhantomGreen
        ),
        modifier = Modifier.testTag(testTag)
      )
    }
  }
}
