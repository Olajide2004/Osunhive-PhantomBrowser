package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AcceptableUseNoticeDialog
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var showNoticeDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var importJsonText by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Settings & Privacy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("App preferences, backup, and legal terms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Status alert banner if present
      uiState.statusMessage?.let { msg ->
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StatusActive.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, StatusActive),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(12.dp)
            ) {
              Text(msg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StatusActive)
              TextButton(onClick = { viewModel.clearStatusMessage() }) {
                Text("Dismiss", fontSize = 11.sp, color = StatusActive)
              }
            }
          }
        }
      }

      // Section 1: Appearance Theme
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (themeKey, label) ->
                FilterChip(
                  selected = uiState.theme == themeKey,
                  onClick = { viewModel.setTheme(themeKey) },
                  label = { Text(label) },
                  leadingIcon = {
                    val icon = when (themeKey) {
                      "DARK" -> Icons.Default.DarkMode
                      "LIGHT" -> Icons.Default.LightMode
                      else -> Icons.Default.Brightness4
                    }
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                    selectedLabelColor = PhantomGreen
                  ),
                  modifier = Modifier.testTag("theme_chip_$themeKey")
                )
              }
            }
          }
        }
      }

      // Section 2: Data Backup & JSON Export/Import
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Profile Backup & Portability", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
              "Export all profiles and fingerprint presets to encrypted or portable JSON schema.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
              Button(
                onClick = { viewModel.exportProfiles() },
                colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("export_profiles_button")
              ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export JSON")
              }

              OutlinedButton(
                onClick = { showImportDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("import_profiles_button")
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import JSON")
              }
            }

            // If JSON was exported, show quick copy area
            uiState.exportedJson?.let { json ->
              Spacer(modifier = Modifier.height(14.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Exported Profiles Payload", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(onClick = {
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      val clip = ClipData.newPlainText("Phantom Profiles", json)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                      Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    }
                  }
                  Text(
                    text = json.take(300) + if (json.length > 300) "..." else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Section 3: Legal & Acceptable Use
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Policy & Compliance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "\"For privacy, testing, and legitimate multi-account management only.\"",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = PhantomGreen
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = { showNoticeDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Review Acceptable Use Notice")
            }
          }
        }
      }

      // Section 4: Support, Donations & Treats (Opay, Telegram, Snacks)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Support Creator • Buy Cookie, Pizzas & Garri", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PhantomGreen)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Donate via Opay & Connect on Telegram (@olajidesherif, @osunhive)",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column {
                    Text("Opay Account:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("9076129380", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                  }
                  IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Opay Account", "9076129380")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Opay number 9076129380 copied!", Toast.LENGTH_SHORT).show()
                  }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Opay", tint = PhantomGreen)
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Phone: 09076129380", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Telegram: @olajidesherif | @osunhive", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PhantomGreen)
              }
            }
          }
        }
      }

      // App Version & Architecture Info
      item {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ) {
          Text(
            text = "PhantomProfile v1.0.0",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Clean Jetpack Compose • Material 3 • Isolated Android WebView Engine",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  if (showNoticeDialog) {
    AcceptableUseNoticeDialog(onAccept = { showNoticeDialog = false })
  }

  if (showImportDialog) {
    AlertDialog(
      onDismissRequest = { showImportDialog = false },
      title = { Text("Import Profiles JSON") },
      text = {
        Column {
          Text("Paste exported JSON profiles string below:", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = importJsonText,
            onValueChange = { importJsonText = it },
            placeholder = { Text("{\"profiles\": [...]}") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("import_json_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (importJsonText.isNotBlank()) {
              viewModel.importProfiles(importJsonText)
              importJsonText = ""
              showImportDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("confirm_import_button")
        ) {
          Text("Import", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showImportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
