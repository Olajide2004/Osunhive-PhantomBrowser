package com.example.ui.proxymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProxyEntity
import com.example.data.proxy.ProxyCheckResult
import com.example.data.proxy.ProxyTester
import com.example.data.repository.ProxyRepository
import com.example.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ParsedProxyData(
  val protocol: String = "HTTP",
  val host: String = "",
  val port: Int = 8080,
  val username: String = "",
  val password: String = "",
  val countryCode: String = "US"
)

data class ProxyManagerUiState(
  val proxies: List<ProxyEntity> = emptyList(),
  val searchQuery: String = "",
  val protocolFilter: String = "All", // "All", "HTTP", "HTTPS", "SOCKS5", "SOCKS4", "Shadowsocks"
  val poolTypeFilter: String = "All", // "All", "Residential", "Datacenter", "Mobile 4G/5G", "Dedicated"
  val showFavoritesOnly: Boolean = false,

  // Add/Edit Form State
  val editingProxyId: String? = null,
  val customProtocol: String = "SOCKS5",
  val customHost: String = "",
  val customPort: String = "1080",
  val customUsername: String = "",
  val customPassword: String = "",
  val customName: String = "",
  val customCountryCode: String = "US",
  val customPoolType: String = "Residential",

  // Testing & Diagnostics
  val isCheckingProxy: Boolean = false,
  val checkResult: ProxyCheckResult? = null,
  val isTestingAll: Boolean = false,
  val testAllProgress: String = "",

  // Quick Paste & CSV Dialog
  val quickPasteText: String = "",
  val showQuickPasteDialog: Boolean = false,
  val showBulkImportDialog: Boolean = false,
  val bulkImportText: String = "",
  val bulkImportResult: String? = null,
  val showCsvUploadDialog: Boolean = false,
  val csvUploadText: String = "",
  val csvUploadResult: String? = null,
  val isAddFormExpanded: Boolean = false
)

class ProxyManagerViewModel(
  private val proxyRepository: ProxyRepository
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  private val _protocolFilter = MutableStateFlow("All")
  private val _poolTypeFilter = MutableStateFlow("All")
  private val _showFavoritesOnly = MutableStateFlow(false)

  // Form states
  private val _editingProxyId = MutableStateFlow<String?>(null)
  private val _customProtocol = MutableStateFlow("SOCKS5")
  private val _customHost = MutableStateFlow("")
  private val _customPort = MutableStateFlow("1080")
  private val _customUsername = MutableStateFlow("")
  private val _customPassword = MutableStateFlow("")
  private val _customName = MutableStateFlow("")
  private val _customCountryCode = MutableStateFlow("US")
  private val _customPoolType = MutableStateFlow("Residential")

  private val _isChecking = MutableStateFlow(false)
  private val _checkResult = MutableStateFlow<ProxyCheckResult?>(null)
  private val _isTestingAll = MutableStateFlow(false)
  private val _testAllProgress = MutableStateFlow("")

  private val _quickPasteText = MutableStateFlow("")
  private val _showQuickPaste = MutableStateFlow(false)
  private val _showBulkImport = MutableStateFlow(false)
  private val _bulkImportText = MutableStateFlow("")
  private val _bulkImportResult = MutableStateFlow<String?>(null)
  private val _showCsvUpload = MutableStateFlow(false)
  private val _csvUploadText = MutableStateFlow("")
  private val _csvUploadResult = MutableStateFlow<String?>(null)
  private val _isAddFormExpanded = MutableStateFlow(false)

  data class FilterOptions(
    val search: String,
    val proto: String,
    val pool: String,
    val favOnly: Boolean
  )

  private val _filterFlow = combine(
    _searchQuery,
    _protocolFilter,
    _poolTypeFilter,
    _showFavoritesOnly,
    ::FilterOptions
  )

  val uiState: StateFlow<ProxyManagerUiState> = combine(
    proxyRepository.allProxies,
    _filterFlow,
    _isChecking,
    _checkResult,
    _isTestingAll
  ) { proxies, filter, isChecking, checkResult, isTestingAll ->
    val filtered = proxies.filter { p ->
      val matchesSearch = filter.search.isBlank() ||
        p.name.contains(filter.search, ignoreCase = true) ||
        p.host.contains(filter.search, ignoreCase = true) ||
        p.port.toString().contains(filter.search) ||
        p.countryName.contains(filter.search, ignoreCase = true) ||
        p.countryCode.contains(filter.search, ignoreCase = true) ||
        p.protocol.contains(filter.search, ignoreCase = true)

      val matchesProtocol = filter.proto == "All" || p.protocol.equals(filter.proto, ignoreCase = true)
      val matchesType = filter.pool == "All" || p.ipPoolType.equals(filter.pool, ignoreCase = true)
      val matchesFav = !filter.favOnly || p.isFavorite

      matchesSearch && matchesProtocol && matchesType && matchesFav
    }

    ProxyManagerUiState(
      proxies = filtered,
      searchQuery = filter.search,
      protocolFilter = filter.proto,
      poolTypeFilter = filter.pool,
      showFavoritesOnly = filter.favOnly,
      editingProxyId = _editingProxyId.value,
      customProtocol = _customProtocol.value,
      customHost = _customHost.value,
      customPort = _customPort.value,
      customUsername = _customUsername.value,
      customPassword = _customPassword.value,
      customName = _customName.value,
      customCountryCode = _customCountryCode.value,
      customPoolType = _customPoolType.value,
      isCheckingProxy = isChecking,
      checkResult = checkResult,
      isTestingAll = isTestingAll,
      testAllProgress = _testAllProgress.value,
      quickPasteText = _quickPasteText.value,
      showQuickPasteDialog = _showQuickPaste.value,
      showBulkImportDialog = _showBulkImport.value,
      bulkImportText = _bulkImportText.value,
      bulkImportResult = _bulkImportResult.value,
      showCsvUploadDialog = _showCsvUpload.value,
      csvUploadText = _csvUploadText.value,
      csvUploadResult = _csvUploadResult.value,
      isAddFormExpanded = _isAddFormExpanded.value
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProxyManagerUiState()
  )

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setProtocolFilter(filter: String) {
    _protocolFilter.value = filter
  }

  fun setPoolTypeFilter(filter: String) {
    _poolTypeFilter.value = filter
  }

  fun toggleFavoritesOnly() {
    _showFavoritesOnly.value = !_showFavoritesOnly.value
  }

  fun toggleProxyFavorite(proxyId: String, currentFavorite: Boolean) {
    viewModelScope.launch {
      proxyRepository.toggleFavorite(proxyId, !currentFavorite)
    }
  }

  fun setAddFormExpanded(expanded: Boolean) {
    _isAddFormExpanded.value = expanded
    if (!expanded) {
      clearForm()
    }
  }

  fun setCustomProtocol(protocol: String) {
    _customProtocol.value = protocol
    // Auto adjust default port if user hasn't typed custom port
    if (_customPort.value in listOf("8080", "8443", "1080", "8388", "22")) {
      _customPort.value = when (protocol.uppercase()) {
        "HTTPS" -> "8443"
        "SOCKS4", "SOCKS4A", "SOCKS5" -> "1080"
        "SHADOWSOCKS" -> "8388"
        "SSH TUNNEL" -> "22"
        else -> "8080"
      }
    }
  }

  fun setCustomHost(host: String) {
    _customHost.value = host
  }

  fun setCustomPort(port: String) {
    _customPort.value = port
  }

  fun setCustomUsername(username: String) {
    _customUsername.value = username
  }

  fun setCustomPassword(password: String) {
    _customPassword.value = password
  }

  fun setCustomName(name: String) {
    _customName.value = name
  }

  fun setCustomCountryCode(country: String) {
    _customCountryCode.value = country.uppercase().trim().take(2)
  }

  fun setCustomPoolType(poolType: String) {
    _customPoolType.value = poolType
  }

  fun showQuickPasteDialog(show: Boolean) {
    _showQuickPaste.value = show
    if (!show) _quickPasteText.value = ""
  }

  fun updateQuickPasteText(text: String) {
    _quickPasteText.value = text
  }

  fun parseAndApplyQuickPaste() {
    val text = _quickPasteText.value.trim()
    val parsed = parseProxyString(text) ?: return

    _customProtocol.value = parsed.protocol
    _customHost.value = parsed.host
    _customPort.value = parsed.port.toString()
    _customUsername.value = parsed.username
    _customPassword.value = parsed.password
    _customCountryCode.value = parsed.countryCode
    if (_customName.value.isBlank()) {
      _customName.value = "${parsed.protocol} ${parsed.host}:${parsed.port}"
    }

    _showQuickPaste.value = false
    _isAddFormExpanded.value = true
  }

  fun showBulkImportDialog(show: Boolean) {
    _showBulkImport.value = show
    if (!show) {
      _bulkImportText.value = ""
      _bulkImportResult.value = null
    }
  }

  fun updateBulkImportText(text: String) {
    _bulkImportText.value = text
  }

  fun executeBulkImport() {
    val lines = _bulkImportText.value.lines()
    val toInsert = mutableListOf<ProxyEntity>()

    for (raw in lines) {
      val parsed = parseProxyString(raw.trim()) ?: continue
      val encPass = if (parsed.password.isNotEmpty()) CryptoManager.encrypt(parsed.password) else ""
      toInsert.add(
        ProxyEntity(
          id = "proxy_${UUID.randomUUID().toString().take(8)}",
          name = "${parsed.protocol} ${parsed.host}:${parsed.port}",
          isRotating = false,
          protocol = parsed.protocol,
          host = parsed.host,
          port = parsed.port,
          username = parsed.username,
          encryptedPassword = encPass,
          countryCode = parsed.countryCode,
          countryName = getCountryName(parsed.countryCode),
          ipPoolType = "Residential",
          latencyMs = -1L,
          lastStatus = "Untested",
          createdAt = System.currentTimeMillis()
        )
      )
    }

    if (toInsert.isNotEmpty()) {
      viewModelScope.launch {
        for (p in toInsert) {
          proxyRepository.insertProxy(p)
        }
        _bulkImportResult.value = "Successfully imported ${toInsert.size} custom proxies!"
      }
    } else {
      _bulkImportResult.value = "No valid proxy formats detected in input text."
    }
  }

  fun showCsvUploadDialog(show: Boolean) {
    _showCsvUpload.value = show
    if (!show) {
      _csvUploadText.value = ""
      _csvUploadResult.value = null
    }
  }

  fun updateCsvUploadText(text: String) {
    _csvUploadText.value = text
  }

  fun executeCsvUpload() {
    val lines = _csvUploadText.value.lines()
    val toInsert = mutableListOf<ProxyEntity>()

    for (raw in lines) {
      val parsed = parseCsvLine(raw.trim()) ?: continue
      val encPass = if (parsed.password.isNotEmpty()) CryptoManager.encrypt(parsed.password) else ""
      toInsert.add(
        ProxyEntity(
          id = "proxy_${UUID.randomUUID().toString().take(8)}",
          name = "${parsed.protocol} ${parsed.host}:${parsed.port}",
          isRotating = false,
          protocol = parsed.protocol,
          host = parsed.host,
          port = parsed.port,
          username = parsed.username,
          encryptedPassword = encPass,
          countryCode = parsed.countryCode,
          countryName = getCountryName(parsed.countryCode),
          ipPoolType = "Residential",
          latencyMs = -1L,
          lastStatus = "Untested",
          createdAt = System.currentTimeMillis()
        )
      )
    }

    if (toInsert.isNotEmpty()) {
      viewModelScope.launch {
        for (p in toInsert) {
          proxyRepository.insertProxy(p)
        }
        _csvUploadResult.value = "Successfully imported ${toInsert.size} proxies from CSV!"
      }
    } else {
      _csvUploadResult.value = "No valid CSV proxy records detected. Format: host,port,user,pass"
    }
  }

  private fun parseCsvLine(line: String): ParsedProxyData? {
    if (line.isBlank() || line.startsWith("#") || (line.contains("host", true) && line.contains("port", true))) {
      return null
    }
    val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
    if (parts.size < 2) return null

    var protocol = "HTTP"
    var host = ""
    var port = 8080
    var username = ""
    var password = ""
    var country = "US"

    if (parts.size >= 5 && (parts[0].equals("HTTP", true) || parts[0].equals("SOCKS5", true) || parts[0].equals("HTTPS", true) || parts[0].equals("SOCKS4", true))) {
      protocol = parts[0].uppercase()
      host = parts[1]
      port = parts[2].toIntOrNull() ?: 8080
      username = parts[3]
      password = parts[4]
      if (parts.size >= 6) country = parts[5].uppercase()
    } else if (parts.size >= 4) {
      host = parts[0]
      port = parts[1].toIntOrNull() ?: 8080
      username = parts[2]
      password = parts[3]
      if (parts.size >= 5 && parts[4].length == 2) country = parts[4].uppercase()
    } else if (parts.size == 2) {
      host = parts[0]
      port = parts[1].toIntOrNull() ?: 8080
    }

    if (host.isBlank()) return null
    return ParsedProxyData(
      protocol = protocol,
      host = host,
      port = port,
      username = username,
      password = password,
      countryCode = country
    )
  }

  fun testSingleProxy() {
    val host = _customHost.value.trim()
    val port = _customPort.value.toIntOrNull() ?: 8080
    val protocol = _customProtocol.value
    val user = _customUsername.value.trim()
    val pass = _customPassword.value

    if (host.isEmpty()) {
      _checkResult.value = ProxyCheckResult(
        success = false,
        latencyMs = 0,
        errorMessage = "Please enter a valid IP address or hostname"
      )
      return
    }

    viewModelScope.launch {
      _isChecking.value = true
      _checkResult.value = null
      val result = ProxyTester.testProxy(protocol, host, port, user, pass)
      _checkResult.value = result
      _isChecking.value = false
    }
  }

  fun testExistingProxy(proxy: ProxyEntity) {
    viewModelScope.launch {
      val decryptedPass = if (proxy.encryptedPassword.isNotEmpty()) {
        CryptoManager.decrypt(proxy.encryptedPassword)
      } else ""
      val result = ProxyTester.testProxy(
        proxy.protocol,
        proxy.host,
        proxy.port,
        proxy.username,
        decryptedPass
      )
      val status = if (result.success) "Active" else "Failed"
      proxyRepository.updateLatency(proxy.id, result.latencyMs, status)
    }
  }

  fun testAllProxies() {
    val list = uiState.value.proxies
    if (list.isEmpty()) return

    viewModelScope.launch(Dispatchers.IO) {
      _isTestingAll.value = true
      _testAllProgress.value = "Pinging 0/${list.size} nodes..."

      var completed = 0
      val jobs = list.map { proxy ->
        async {
          val decryptedPass = if (proxy.encryptedPassword.isNotEmpty()) {
            CryptoManager.decrypt(proxy.encryptedPassword)
          } else ""
          val result = ProxyTester.testProxy(
            proxy.protocol,
            proxy.host,
            proxy.port,
            proxy.username,
            decryptedPass
          )
          val status = if (result.success) "Active" else "Failed"
          proxyRepository.updateLatency(proxy.id, result.latencyMs, status)
          completed++
          _testAllProgress.value = "Pinging $completed/${list.size} nodes..."
        }
      }
      jobs.awaitAll()
      _isTestingAll.value = false
      _testAllProgress.value = "All ${list.size} proxies tested!"
    }
  }

  fun startEditProxy(proxy: ProxyEntity) {
    _editingProxyId.value = proxy.id
    _customProtocol.value = proxy.protocol
    _customHost.value = proxy.host
    _customPort.value = proxy.port.toString()
    _customUsername.value = proxy.username
    _customPassword.value = if (proxy.encryptedPassword.isNotEmpty()) CryptoManager.decrypt(proxy.encryptedPassword) else ""
    _customName.value = proxy.name
    _customCountryCode.value = proxy.countryCode
    _customPoolType.value = proxy.ipPoolType
    _checkResult.value = null
    _isAddFormExpanded.value = true
  }

  fun saveCustomProxy(onSaved: () -> Unit = {}) {
    val host = _customHost.value.trim()
    val port = _customPort.value.toIntOrNull() ?: 8080
    val protocol = _customProtocol.value
    val user = _customUsername.value.trim()
    val pass = _customPassword.value
    val name = if (_customName.value.isNotBlank()) _customName.value.trim() else "$protocol $host:$port"
    val country = if (_customCountryCode.value.isNotBlank()) _customCountryCode.value.trim().uppercase() else "US"
    val poolType = _customPoolType.value

    if (host.isEmpty()) return

    viewModelScope.launch {
      val encryptedPass = if (pass.isNotEmpty()) CryptoManager.encrypt(pass) else ""
      val latency = _checkResult.value?.latencyMs ?: -1L
      val status = if (_checkResult.value?.success == true) "Active" else "Untested"

      val editingId = _editingProxyId.value
      val proxy = ProxyEntity(
        id = editingId ?: UUID.randomUUID().toString(),
        name = name,
        isRotating = false,
        protocol = protocol,
        host = host,
        port = port,
        username = user,
        encryptedPassword = encryptedPass,
        countryCode = country,
        countryName = getCountryName(country),
        ipPoolType = poolType,
        latencyMs = latency,
        lastStatus = status,
        createdAt = System.currentTimeMillis()
      )

      if (editingId != null) {
        proxyRepository.updateProxy(proxy)
      } else {
        proxyRepository.insertProxy(proxy)
      }

      clearForm()
      onSaved()
    }
  }

  fun deleteProxy(proxyId: String) {
    viewModelScope.launch {
      proxyRepository.deleteProxy(proxyId)
    }
  }

  fun duplicateProxy(proxy: ProxyEntity) {
    viewModelScope.launch {
      val clone = proxy.copy(
        id = UUID.randomUUID().toString(),
        name = "${proxy.name} (Copy)",
        latencyMs = -1L,
        lastStatus = "Untested",
        createdAt = System.currentTimeMillis()
      )
      proxyRepository.insertProxy(clone)
    }
  }

  fun clearForm() {
    _editingProxyId.value = null
    _customProtocol.value = "SOCKS5"
    _customHost.value = ""
    _customPort.value = "1080"
    _customUsername.value = ""
    _customPassword.value = ""
    _customName.value = ""
    _customCountryCode.value = "US"
    _customPoolType.value = "Residential"
    _checkResult.value = null
    _isAddFormExpanded.value = false
  }

  private fun parseProxyString(raw: String): ParsedProxyData? {
    if (raw.isBlank() || raw.startsWith("#")) return null
    var text = raw.trim()

    var protocol = "HTTP"
    if (text.contains("://")) {
      val proto = text.substringBefore("://").uppercase()
      protocol = when (proto) {
        "HTTPS", "SSL" -> "HTTPS"
        "SOCKS4" -> "SOCKS4"
        "SOCKS4A" -> "SOCKS4A"
        "SOCKS5", "SOCKS" -> "SOCKS5"
        "SS", "SHADOWSOCKS" -> "Shadowsocks"
        "SSH" -> "SSH Tunnel"
        else -> "HTTP"
      }
      text = text.substringAfter("://")
    }

    var username = ""
    var password = ""
    var host = ""
    var port = 8080

    if (text.contains("@")) {
      // username:password@host:port
      val authPart = text.substringBefore("@")
      val hostPart = text.substringAfter("@")
      username = authPart.substringBefore(":")
      password = if (authPart.contains(":")) authPart.substringAfter(":") else ""

      host = hostPart.substringBefore(":")
      port = hostPart.substringAfter(":").toIntOrNull() ?: 8080
    } else {
      // Could be host:port or host:port:user:pass
      val parts = text.split(":")
      when (parts.size) {
        2 -> {
          host = parts[0]
          port = parts[1].toIntOrNull() ?: 8080
        }
        4 -> {
          host = parts[0]
          port = parts[1].toIntOrNull() ?: 8080
          username = parts[2]
          password = parts[3]
        }
        else -> {
          host = text
        }
      }
    }

    if (host.isBlank()) return null
    return ParsedProxyData(
      protocol = protocol,
      host = host,
      port = port,
      username = username,
      password = password,
      countryCode = "US"
    )
  }

  private fun getCountryName(code: String): String {
    return when (code.uppercase()) {
      "US" -> "United States"
      "DE" -> "Germany"
      "GB" -> "United Kingdom"
      "FR" -> "France"
      "JP" -> "Japan"
      "CA" -> "Canada"
      "SG" -> "Singapore"
      "NL" -> "Netherlands"
      "AU" -> "Australia"
      "CH" -> "Switzerland"
      "SE" -> "Sweden"
      "BR" -> "Brazil"
      "IN" -> "India"
      else -> code.uppercase()
    }
  }

  class Factory(
    private val proxyRepository: ProxyRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ProxyManagerViewModel(proxyRepository) as T
    }
  }
}
