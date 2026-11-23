package com.example.ui.browser

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProfileEntity
import com.example.ui.common.OsIconBadge
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTabPage(
  profile: ProfileEntity,
  onNavigateToUrl: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedEngine by remember { mutableStateOf("DuckDuckGo") }
  var showEngineMenu by remember { mutableStateOf(false) }

  val searchEngines = listOf(
    "DuckDuckGo" to "https://duckduckgo.com/?q=",
    "Google" to "https://www.google.com/search?q=",
    "Startpage" to "https://www.startpage.com/do/search?q=",
    "Bing" to "https://www.bing.com/search?q="
  )

  fun executeSearch(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      onNavigateToUrl(trimmed)
    } else if (trimmed.contains(".") && !trimmed.contains(" ")) {
      onNavigateToUrl("https://$trimmed")
    } else {
      val base = searchEngines.find { it.first == selectedEngine }?.second ?: "https://duckduckgo.com/?q="
      onNavigateToUrl(base + java.net.URLEncoder.encode(trimmed, "UTF-8"))
    }
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp)
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // Phantom Chrome Stealth Logo
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = PhantomGreen.copy(alpha = 0.15f),
        border = BorderStroke(2.dp, PhantomGreen.copy(alpha = 0.5f)),
        modifier = Modifier.size(56.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = PhantomGreen,
            modifier = Modifier.size(32.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(
          text = "Phantom Chrome",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Isolated Sandbox Browser",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = PhantomGreen
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Chrome Search / Omnibox Pill
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
      ) {
        Box {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            onClick = { showEngineMenu = true },
            modifier = Modifier.testTag("search_engine_selector")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Text(
                text = when (selectedEngine) {
                  "DuckDuckGo" -> "🦆"
                  "Google" -> "🔍"
                  "Startpage" -> "🔒"
                  else -> "🌐"
                },
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(selectedEngine, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
          DropdownMenu(
            expanded = showEngineMenu,
            onDismissRequest = { showEngineMenu = false }
          ) {
            searchEngines.forEach { (engine, _) ->
              DropdownMenuItem(
                text = { Text(engine) },
                onClick = {
                  selectedEngine = engine
                  showEngineMenu = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search or type web address", fontSize = 14.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { executeSearch(searchQuery) }),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = PhantomGreen
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("ntp_search_input")
        )

        IconButton(
          onClick = { executeSearch(searchQuery) },
          modifier = Modifier.testTag("ntp_search_submit_button")
        ) {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = PhantomGreen)
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Speed Dial Shortcuts (Chrome Tiles)
    Text(
      text = "Stealth Testing & Shortcuts",
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(12.dp))

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      maxItemsInEachRow = 4,
      modifier = Modifier.fillMaxWidth()
    ) {
      DEFAULT_SPEED_DIALS.forEach { item ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNavigateToUrl(item.url) }
            .padding(vertical = 4.dp)
            .testTag("speed_dial_${item.title}")
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.size(52.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = item.iconEmoji, fontSize = 22.sp)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Active Profile Stealth Specs Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      ),
      border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            OsIconBadge(os = profile.os, tint = PhantomGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = profile.name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StatusActive.copy(alpha = 0.15f)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(StatusActive)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "SANDBOX ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StatusActive
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text("Proxy Node", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              "${getCountryFlagEmoji(profile.proxyCountry)} ${if (profile.proxyCountry == "DIRECT") "Direct" else profile.proxyCountry}",
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
          }
          Column {
            Text("Resolution", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${profile.resolutionWidth}x${profile.resolutionHeight}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
          Column {
            Text("Hardware", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${profile.cpuThreads} CPU • ${profile.deviceMemoryGb}GB", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          StatusTag("Canvas Noise", profile.canvasNoiseEnabled)
          StatusTag("WebGL Spoof", profile.webGlSpoofing)
          StatusTag("Audio Jitter", profile.audioNoiseEnabled)
          StatusTag("Virtual Cam", profile.virtualCameraEnabled)
        }
      }
    }
  }
}

@Composable
fun StatusTag(label: String, active: Boolean) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (active) PhantomGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
  ) {
    Text(
      text = if (active) "✓ $label" else "✗ $label",
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (active) PhantomGreen else Color.Gray,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}
