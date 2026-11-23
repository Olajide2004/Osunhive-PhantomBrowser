package com.example.ui.profiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProfileEntity
import com.example.ui.common.AcceptableUseNoticeDialog
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.PhantomGreenDark
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
  viewModel: ProfileListViewModel,
  onLaunchProfile: (ProfileEntity) -> Unit,
  onOpenExtendedSettings: (String) -> Unit,
  onNavigateToProxyManager: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToCreateProfile: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showAddBottomSheet by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var newFolderName by remember { mutableStateOf("") }

  // Acceptable-use notice check
  if (!uiState.noticeAccepted) {
    AcceptableUseNoticeDialog(
      onAccept = { viewModel.acceptNotice() }
    )
  }

  Scaffold(
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.background)
      ) {
        // App / Workspace Bar
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = PhantomGreen.copy(alpha = 0.15f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = PhantomGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Phantom Workspace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "${uiState.totalProfilesCount} Profiles Managed",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Active indicator pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.testTag("workspace_status_pill")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (uiState.runningProfileId != null) PhantomGreen else Color.Gray)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (uiState.runningProfileId != null) "1 Active Session" else "Stealth Guard ON",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Search Bar
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = { viewModel.updateSearchQuery(it) },
          placeholder = { Text("Search profiles, notes, country...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PhantomGreen,
            cursorColor = PhantomGreen
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("profile_search_input")
        )

        // Folder Filter Chips
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          FilterChip(
            selected = uiState.selectedFolderId == null,
            onClick = { viewModel.selectFolder(null) },
            label = { Text("All (${uiState.totalProfilesCount})") },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
              selectedLabelColor = PhantomGreen
            ),
            border = BorderStroke(
              1.dp,
              if (uiState.selectedFolderId == null) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag("folder_chip_all")
          )

          Spacer(modifier = Modifier.width(8.dp))

          uiState.folders.forEach { folder ->
            FilterChip(
              selected = uiState.selectedFolderId == folder.id,
              onClick = { viewModel.selectFolder(folder.id) },
              label = { Text(folder.name) },
              shape = RoundedCornerShape(10.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                selectedLabelColor = PhantomGreen
              ),
              border = BorderStroke(
                1.dp,
                if (uiState.selectedFolderId == folder.id) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              ),
              modifier = Modifier.testTag("folder_chip_${folder.id}")
            )
            Spacer(modifier = Modifier.width(8.dp))
          }

          // Add Folder Chip
          Surface(
            onClick = { showNewFolderDialog = true },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.testTag("add_folder_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CreateNewFolder,
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
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNavigateToCreateProfile,
        containerColor = PhantomGreen,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("add_profile_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Profile", modifier = Modifier.size(28.dp))
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = paddingValues.calculateTopPadding() + 8.dp,
        bottom = paddingValues.calculateBottomPadding() + 80.dp
      ),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Dismissible Trial Banner
      if (!uiState.isTrialBannerDismissed) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = PhantomGreen.copy(alpha = 0.12f)
            ),
            border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.4f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("trial_banner")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(14.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Surface(
                  shape = CircleShape,
                  color = PhantomGreen,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.VerifiedUser,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "PhantomProfile Pro Trial • 14 Days Left",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Unlimited isolated sessions, canvas noise & proxy switching active.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(
                onClick = { viewModel.dismissTrialBanner() },
                modifier = Modifier.testTag("dismiss_trial_banner")
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss Banner",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      // Profiles List
      if (uiState.profiles.isEmpty()) {
        item {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 48.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Profiles Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Create an isolated browser environment with custom proxy & privacy.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onNavigateToCreateProfile,
                colors = ButtonDefaults.buttonColors(
                  containerColor = PhantomGreen,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_create_profile_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Isolated Profile", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        items(uiState.profiles, key = { it.id }) { profile ->
          ProfileItemCard(
            profile = profile,
            onLaunch = { viewModel.launchProfile(profile, onLaunchProfile) },
            onStop = { viewModel.stopProfile(profile.id) },
            onOpenExtendedSettings = { onOpenExtendedSettings(profile.id) },
            onDuplicate = { viewModel.duplicateProfile(profile) },
            onDelete = { viewModel.deleteProfile(profile.id) }
          )
        }
      }
    }
  }

  // Add Profile Bottom Sheet
  if (showAddBottomSheet) {
    AddProfileBottomSheet(
      folders = uiState.folders,
      proxies = uiState.proxies,
      onDismiss = { showAddBottomSheet = false },
      onAddProfile = { name, os, proxyId, folderId ->
        viewModel.quickCreateProfile(name, os, proxyId, folderId)
        showAddBottomSheet = false
      },
      onNavigateToExtended = {
        showAddBottomSheet = false
        onNavigateToCreateProfile()
      }
    )
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
          placeholder = { Text("e.g. Finance, Ad Spying") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("folder_name_input")
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderName.isNotBlank()) {
              viewModel.createFolder(newFolderName, "#00A98A")
              newFolderName = ""
              showNewFolderDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("create_folder_submit_button")
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
