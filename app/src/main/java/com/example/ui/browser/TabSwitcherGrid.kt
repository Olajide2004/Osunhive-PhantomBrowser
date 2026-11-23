package com.example.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherGrid(
  tabs: List<BrowserTab>,
  selectedTabId: String,
  onSelectTab: (String) -> Unit,
  onCloseTab: (String) -> Unit,
  onNewTab: () -> Unit,
  onCloseAllTabs: () -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "${tabs.size} open ${if (tabs.size == 1) "tab" else "tabs"}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        },
        actions = {
          IconButton(
            onClick = onNewTab,
            modifier = Modifier.testTag("tab_switcher_new_tab_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Tab", tint = PhantomGreen)
          }

          if (tabs.size > 1) {
            IconButton(
              onClick = onCloseAllTabs,
              modifier = Modifier.testTag("tab_switcher_close_all_button")
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Close All Tabs")
            }
          }

          TextButton(
            onClick = onDone,
            modifier = Modifier.testTag("tab_switcher_done_button")
          ) {
            Text("Done", fontWeight = FontWeight.Bold, color = PhantomGreen)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      items(tabs, key = { it.id }) { tab ->
        val isSelected = tab.id == selectedTabId

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onSelectTab(tab.id) }
            .testTag("tab_card_${tab.id}")
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header (Favicon + Title + Close Button)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  if (isSelected) PhantomGreen.copy(alpha = 0.12f)
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Text(text = tab.faviconEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = tab.title,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              IconButton(
                onClick = { onCloseTab(tab.id) },
                modifier = Modifier
                  .size(24.dp)
                  .testTag("close_tab_${tab.id}")
              ) {
                Icon(
                  Icons.Default.Close,
                  contentDescription = "Close tab",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            // Tab Body Snapshot Preview
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(12.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                  modifier = Modifier.size(42.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(text = tab.faviconEmoji, fontSize = 20.sp)
                  }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = if (tab.url == "chrome://newtab") "New Tab" else tab.url.removePrefix("https://").removePrefix("http://"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
  }
}
