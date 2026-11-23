package com.example.ui.profiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fingerprint.FingerprintGenerator
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import com.example.ui.common.OsIconBadge
import com.example.ui.common.StatusBadge
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusStopped

@Composable
fun ProfileItemCard(
  profile: ProfileEntity,
  onLaunch: () -> Unit,
  onStop: () -> Unit,
  onOpenExtendedSettings: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      1.dp,
      if (profile.isRunning) PhantomGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("profile_card_${profile.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top Row: Name, OS Icon, Status Badge, More Menu
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              OsIconBadge(
                os = profile.os,
                tint = if (profile.isRunning) PhantomGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = profile.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${profile.os} • ${profile.resolutionWidth}x${profile.resolutionHeight}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          StatusBadge(isRunning = profile.isRunning)
          Box {
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.testTag("profile_menu_button_${profile.id}")
            ) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Profile Options")
            }
            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Extended Settings") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                onClick = {
                  menuExpanded = false
                  onOpenExtendedSettings()
                }
              )
              DropdownMenuItem(
                text = { Text("Duplicate Profile") },
                leadingIcon = { Icon(Icons.Default.FileCopy, contentDescription = null) },
                onClick = {
                  menuExpanded = false
                  onDuplicate()
                }
              )
              DropdownMenuItem(
                text = { Text("Delete Profile", color = StatusError) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError) },
                onClick = {
                  menuExpanded = false
                  onDelete()
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Middle Row: Proxy Info & Data usage
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = getCountryFlagEmoji(profile.proxyCountry), fontSize = 16.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (profile.proxyCountry == "DIRECT") "Direct Connection" else "Proxy: ${profile.proxyCountry}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = "%.1f MB".format(profile.dataUsedMb),
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (profile.note.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = profile.note,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Row: Launch / Stop and Extended Settings Shortcut
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (profile.isRunning) {
          Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
              containerColor = StatusError.copy(alpha = 0.15f),
              contentColor = StatusError
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("stop_profile_button_${profile.id}")
          ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stop Session", fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = onLaunch,
            colors = ButtonDefaults.buttonColors(
              containerColor = PhantomGreen,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("launch_profile_button_${profile.id}")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Launch Browser", fontWeight = FontWeight.Bold)
          }
        }

        OutlinedButton(
          onClick = onOpenExtendedSettings,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.testTag("extended_settings_button_${profile.id}")
        ) {
          Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddProfileBottomSheet(
  folders: List<FolderEntity>,
  proxies: List<ProxyEntity>,
  onDismiss: () -> Unit,
  onAddProfile: (name: String, os: String, proxyId: String?, folderId: String?) -> Unit,
  onNavigateToExtended: () -> Unit
) {
  var name by remember { mutableStateOf(FingerprintGenerator.generateRandomName()) }
  var selectedOs by remember { mutableStateOf("Android") }
  var selectedProxyId by remember { mutableStateOf<String?>(null) }
  var selectedFolderId by remember { mutableStateOf<String?>(null) }

  val osOptions = listOf("Android", "Windows", "macOS", "Linux", "iOS")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .padding(bottom = 24.dp)
    ) {
      Text(
        text = "New Stealth Profile",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Create an isolated browser environment with custom hardware entropy.",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Profile Name with Randomize Dice Button
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Profile Name") },
        trailingIcon = {
          IconButton(
            onClick = { name = FingerprintGenerator.generateRandomName() },
            modifier = Modifier.testTag("randomize_name_button")
          ) {
            Icon(Icons.Default.Casino, contentDescription = "Randomize Name", tint = PhantomGreen)
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

      Spacer(modifier = Modifier.height(16.dp))

      // OS Selector Chips
      Text(
        text = "Operating System Emulation",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        osOptions.forEach { os ->
          FilterChip(
            selected = selectedOs == os,
            onClick = { selectedOs = os },
            label = { Text(os) },
            leadingIcon = { OsIconBadge(os = os) },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
              selectedLabelColor = PhantomGreen
            ),
            border = BorderStroke(
              1.dp,
              if (selectedOs == os) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Proxy Selection
      Text(
        text = "Proxy Node",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedProxyId == null,
          onClick = { selectedProxyId = null },
          label = { Text("🌐 Direct (No Proxy)") },
          shape = RoundedCornerShape(10.dp)
        )
        proxies.take(5).forEach { proxy ->
          FilterChip(
            selected = selectedProxyId == proxy.id,
            onClick = { selectedProxyId = proxy.id },
            label = { Text("${getCountryFlagEmoji(proxy.countryCode)} ${proxy.name}") },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Folder Selector
      if (folders.isNotEmpty()) {
        Text(
          text = "Workspace Folder",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedFolderId == null,
            onClick = { selectedFolderId = null },
            label = { Text("None") },
            shape = RoundedCornerShape(10.dp)
          )
          folders.forEach { folder ->
            FilterChip(
              selected = selectedFolderId == folder.id,
              onClick = { selectedFolderId = folder.id },
              label = { Text(folder.name) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Extended Settings row button
      Surface(
        onClick = onNavigateToExtended,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("extended_settings_row")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = PhantomGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Proxy & Privacy Studio", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
              Text("Fine-tune proxy routing, canvas noise, WebRTC & sandbox", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Add Profile Button
      Button(
        onClick = {
          onAddProfile(name, selectedOs, selectedProxyId, selectedFolderId)
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PhantomGreen,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("add_profile_submit_button")
      ) {
        Text("Add Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
