package com.example.ui.proxymanager

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProxyEntity
import com.example.ui.common.getCountryFlagEmoji
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProxyManagerScreen(
  viewModel: ProxyManagerViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showPassword by remember { mutableStateOf(false) }
  val clipboardManager = LocalClipboardManager.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Custom Proxy Manager", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
              "HTTP, HTTPS, SOCKS4, SOCKS5 & Shadowsocks",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          // Quick Paste button
          IconButton(
            onClick = { viewModel.showQuickPasteDialog(true) },
            modifier = Modifier.testTag("quick_paste_button")
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = "Quick Paste String", tint = PhantomGreen)
          }

          // Bulk Import button
          IconButton(
            onClick = { viewModel.showBulkImportDialog(true) },
            modifier = Modifier.testTag("bulk_import_button")
          ) {
            Icon(Icons.Default.PlaylistAdd, contentDescription = "Bulk Import Proxies")
          }

          // CSV Upload button
          IconButton(
            onClick = { viewModel.showCsvUploadDialog(true) },
            modifier = Modifier.testTag("upload_csv_button")
          ) {
            Icon(Icons.Default.Description, contentDescription = "Upload CSV Proxies", tint = PhantomGreen)
          }

          // Test All button
          IconButton(
            onClick = { viewModel.testAllProxies() },
            enabled = !uiState.isTestingAll && uiState.proxies.isNotEmpty(),
            modifier = Modifier.testTag("test_all_proxies_button")
          ) {
            if (uiState.isTestingAll) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PhantomGreen)
            } else {
              Icon(Icons.Default.Speed, contentDescription = "Test All Proxies", tint = PhantomGreen)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      if (!uiState.isAddFormExpanded) {
        Button(
          onClick = { viewModel.setAddFormExpanded(true) },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen, contentColor = Color.White),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("add_custom_proxy_fab")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Custom Proxy", fontWeight = FontWeight.Bold)
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
        bottom = paddingValues.calculateBottomPadding() + 80.dp
      ),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Test all progress banner
      if (uiState.isTestingAll || uiState.testAllProgress.isNotEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = PhantomGreen.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, PhantomGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = PhantomGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = uiState.testAllProgress.ifEmpty { "Testing proxy nodes..." },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PhantomGreen
              )
            }
          }
        }
      }

      // Add / Edit Proxy Form Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, if (uiState.editingProxyId != null) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
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
                  color = PhantomGreen.copy(alpha = 0.15f),
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = if (uiState.editingProxyId != null) Icons.Default.Edit else Icons.Default.Add,
                      contentDescription = null,
                      tint = PhantomGreen,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (uiState.editingProxyId != null) "Edit Custom Proxy" else "Add New Custom Proxy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "Supports SOCKS5, HTTP, HTTPS, SOCKS4, Shadowsocks",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(onClick = { viewModel.setAddFormExpanded(!uiState.isAddFormExpanded) }) {
                Icon(
                  imageVector = if (uiState.isAddFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                  contentDescription = "Toggle Form"
                )
              }
            }

            AnimatedVisibility(
              visible = uiState.isAddFormExpanded,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()
            ) {
              Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp)
              ) {
                // Protocol selector chips
                Text("Proxy Protocol", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                val protocols = listOf("SOCKS5", "HTTP", "HTTPS", "SOCKS4", "Shadowsocks", "SSH Tunnel")
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  protocols.forEach { proto ->
                    FilterChip(
                      selected = uiState.customProtocol.equals(proto, ignoreCase = true),
                      onClick = { viewModel.setCustomProtocol(proto) },
                      label = { Text(proto) },
                      shape = RoundedCornerShape(8.dp),
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                        selectedLabelColor = PhantomGreen
                      ),
                      border = BorderStroke(
                        1.dp,
                        if (uiState.customProtocol.equals(proto, ignoreCase = true)) PhantomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                      ),
                      modifier = Modifier.testTag("form_proto_$proto")
                    )
                  }
                }

                // IP Category selector chips
                Text("IP Category / Pool Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                val poolTypes = listOf("Residential", "Datacenter", "Mobile 4G/5G", "Dedicated")
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  poolTypes.forEach { pool ->
                    FilterChip(
                      selected = uiState.customPoolType == pool,
                      onClick = { viewModel.setCustomPoolType(pool) },
                      label = { Text(pool) },
                      shape = RoundedCornerShape(8.dp),
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                        selectedLabelColor = PhantomGreen
                      ),
                      modifier = Modifier.testTag("form_pool_$pool")
                    )
                  }
                }

                // Name input
                OutlinedTextField(
                  value = uiState.customName,
                  onValueChange = { viewModel.setCustomName(it) },
                  label = { Text("Proxy Name / Tag") },
                  placeholder = { Text("e.g. Frankfurt Fast SOCKS5, Ad Spying Node") },
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_proxy_name_input")
                )

                // Host & Port
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                    value = uiState.customHost,
                    onValueChange = { viewModel.setCustomHost(it) },
                    label = { Text("Host / IP Address") },
                    placeholder = { Text("192.168.1.1 or proxy.example.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(2f)
                      .testTag("custom_proxy_host_input")
                  )

                  OutlinedTextField(
                    value = uiState.customPort,
                    onValueChange = { viewModel.setCustomPort(it) },
                    label = { Text("Port") },
                    placeholder = { Text("1080") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("custom_proxy_port_input")
                  )
                }

                // Username & Password
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                    value = uiState.customUsername,
                    onValueChange = { viewModel.setCustomUsername(it) },
                    label = { Text("Username (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("custom_proxy_user_input")
                  )

                  OutlinedTextField(
                    value = uiState.customPassword,
                    onValueChange = { viewModel.setCustomPassword(it) },
                    label = { Text("Password (Optional)") },
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

                // Country Code
                OutlinedTextField(
                  value = uiState.customCountryCode,
                  onValueChange = { viewModel.setCustomCountryCode(it) },
                  label = { Text("Exit Country Code (2-letter ISO)") },
                  placeholder = { Text("US, DE, GB, FR, JP...") },
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_proxy_country_input")
                )

                // Diagnostics Check Result
                uiState.checkResult?.let { result ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (result.success) StatusActive.copy(alpha = 0.12f) else StatusError.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (result.success) StatusActive.copy(alpha = 0.4f) else StatusError.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (result.success) StatusActive else StatusError)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = if (result.success) "Handshake Succeeded (${result.latencyMs}ms)" else "Connection Failed",
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          color = if (result.success) StatusActive else StatusError
                        )
                      }
                      if (result.success && result.detectedIp.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Verified Exit IP: ${result.detectedIp}",
                          fontSize = 11.sp,
                          fontFamily = FontFamily.Monospace,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                      }
                      result.errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = msg,
                          fontSize = 11.sp,
                          color = StatusError
                        )
                      }
                    }
                  }
                }

                // Form Buttons
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  OutlinedButton(
                    onClick = { viewModel.testSingleProxy() },
                    enabled = !uiState.isCheckingProxy,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("test_custom_proxy_button")
                  ) {
                    if (uiState.isCheckingProxy) {
                      CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                      Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Test Ping")
                    }
                  }

                  Button(
                    onClick = { viewModel.saveCustomProxy() },
                    colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("save_custom_proxy_button")
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.editingProxyId != null) "Update" else "Save Proxy", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      // Search & Filters
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Search Input
          OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search by name, host, port, country...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
              if (uiState.searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("proxy_search_input")
          )

          // Filter row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            FilterChip(
              selected = uiState.showFavoritesOnly,
              onClick = { viewModel.toggleFavoritesOnly() },
              label = { Text("★ Favorites") },
              shape = RoundedCornerShape(8.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                selectedLabelColor = PhantomGreen
              ),
              modifier = Modifier.testTag("filter_favorites")
            )
            Spacer(modifier = Modifier.width(8.dp))

            val protocolChips = listOf("All", "SOCKS5", "HTTP", "HTTPS", "SOCKS4", "Shadowsocks")
            protocolChips.forEach { proto ->
              FilterChip(
                selected = uiState.protocolFilter == proto,
                onClick = { viewModel.setProtocolFilter(proto) },
                label = { Text(proto) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PhantomGreen.copy(alpha = 0.2f),
                  selectedLabelColor = PhantomGreen
                ),
                modifier = Modifier.testTag("filter_proto_$proto")
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
          }
        }
      }

      // Proxies List Section Header
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Configured Nodes (${uiState.proxies.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "SOCKS4/5 • HTTP/S • Shadowsocks",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Proxies List
      if (uiState.proxies.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Icon(Icons.Default.Public, contentDescription = null, tint = PhantomGreen, modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("No Custom Proxies Found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Tap 'Add Custom Proxy' or 'Quick Paste' to configure custom SOCKS5, HTTP, HTTPS, SOCKS4, or Shadowsocks proxies.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }
          }
        }
      } else {
        items(uiState.proxies, key = { it.id }) { proxy ->
          CustomProxyCard(
            proxy = proxy,
            onTest = { viewModel.testExistingProxy(proxy) },
            onFavorite = { viewModel.toggleProxyFavorite(proxy.id, proxy.isFavorite) },
            onEdit = { viewModel.startEditProxy(proxy) },
            onDuplicate = { viewModel.duplicateProxy(proxy) },
            onDelete = { viewModel.deleteProxy(proxy.id) },
            onCopy = {
              val str = "${proxy.protocol.lowercase()}://${proxy.host}:${proxy.port}"
              clipboardManager.setText(AnnotatedString(str))
            }
          )
        }
      }
    }
  }

  // Quick Paste Dialog
  if (uiState.showQuickPasteDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.showQuickPasteDialog(false) },
      title = { Text("Quick Paste Proxy String") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Supports standard proxy formats:\n• protocol://user:pass@host:port\n• host:port:user:pass\n• socks5://host:port\n• host:port",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = uiState.quickPasteText,
            onValueChange = { viewModel.updateQuickPasteText(it) },
            placeholder = { Text("socks5://user:pass@192.168.1.100:1080") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("quick_paste_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.parseAndApplyQuickPaste() },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("quick_paste_confirm_button")
        ) {
          Text("Parse & Fill", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showQuickPasteDialog(false) }) {
          Text("Cancel")
        }
      }
    )
  }

  // Bulk Import Dialog
  if (uiState.showBulkImportDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.showBulkImportDialog(false) },
      title = { Text("Bulk Import Proxies") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Paste multiple proxies below (one proxy per line):",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = uiState.bulkImportText,
            onValueChange = { viewModel.updateBulkImportText(it) },
            placeholder = {
              Text("104.28.19.45:1080:user:pass\nsocks5://185.190.140.22:1080\nhttp://51.159.20.12:8080")
            },
            minLines = 5,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("bulk_import_input")
          )
          uiState.bulkImportResult?.let { res ->
            Text(res, fontSize = 12.sp, color = PhantomGreen, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.executeBulkImport() },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("bulk_import_confirm_button")
        ) {
          Text("Import All", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showBulkImportDialog(false) }) {
          Text("Close")
        }
      }
    )
  }

  // CSV Upload Dialog
  if (uiState.showCsvUploadDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.showCsvUploadDialog(false) },
      title = { Text("Upload CSV Proxies") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Paste CSV proxy rows (host,port,username,password or protocol,host,port,username,password):",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = uiState.csvUploadText,
            onValueChange = { viewModel.updateCsvUploadText(it) },
            placeholder = {
              Text("104.28.19.45,1080,user1,pass1\nsocks5,185.190.140.22,1080,user2,pass2")
            },
            minLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("csv_upload_input")
          )
          uiState.csvUploadResult?.let { res ->
            Text(res, fontSize = 12.sp, color = PhantomGreen, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.executeCsvUpload() },
          colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
          modifier = Modifier.testTag("csv_upload_confirm_button")
        ) {
          Text("Import CSV", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showCsvUploadDialog(false) }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun CustomProxyCard(
  proxy: ProxyEntity,
  onTest: () -> Unit,
  onFavorite: () -> Unit,
  onEdit: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit,
  onCopy: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("custom_proxy_card_${proxy.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(getCountryFlagEmoji(proxy.countryCode), fontSize = 22.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(proxy.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              "${proxy.protocol} • ${proxy.ipPoolType} • ${proxy.countryName}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = if (proxy.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (proxy.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Host:Port Pill with Copy Action
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "${proxy.host}:${proxy.port}",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Proxy", modifier = Modifier.size(14.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Latency & Action row
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Status & Latency indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                when (proxy.lastStatus) {
                  "Active" -> StatusActive
                  "Failed" -> StatusError
                  else -> Color.Gray
                }
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (proxy.latencyMs > 0) "${proxy.latencyMs}ms (${proxy.lastStatus})" else proxy.lastStatus,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = when (proxy.lastStatus) {
              "Active" -> StatusActive
              "Failed" -> StatusError
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
          )
        }

        // Action Buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Ping test button
          IconButton(onClick = onTest, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Speed, contentDescription = "Test Latency", tint = PhantomGreen, modifier = Modifier.size(18.dp))
          }

          // Edit button
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
          }

          // Delete button
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}
