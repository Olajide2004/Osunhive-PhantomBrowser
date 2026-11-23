package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.PhantomApplication
import com.example.data.local.entity.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.ui.browser.BrowserSessionScreen
import com.example.ui.extendedsettings.ExtendedSettingsScreen
import com.example.ui.extendedsettings.ExtendedSettingsViewModel
import com.example.ui.profiles.ProfileListScreen
import com.example.ui.profiles.ProfileListViewModel
import com.example.ui.proxymanager.ProxyManagerScreen
import com.example.ui.proxymanager.ProxyManagerViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.profiles.create.CreateProfileScreen
import com.example.ui.profiles.create.CreateProfileViewModel
import com.example.ui.multiwebview.MultiWebViewGridScreen
import androidx.compose.material.icons.filled.Tab
import com.example.ui.theme.PhantomGreen

object NavRoutes {
  const val PROFILES = "profiles"
  const val CREATE_PROFILE = "create_profile"
  const val PROXY_MANAGER = "proxy_manager"
  const val SETTINGS = "settings"
  const val MULTI_WEBVIEW = "multi_webview"
  const val EXTENDED_SETTINGS = "extended_settings/{profileId}"
  const val BROWSER_SESSION = "browser_session"

  fun extendedSettings(profileId: String) = "extended_settings/$profileId"
}

@Composable
fun PhantomMainApp(
  navController: NavHostController = rememberNavController()
) {
  val context = LocalContext.current
  val app = context.applicationContext as PhantomApplication

  val profileListViewModel: ProfileListViewModel = viewModel(
    factory = ProfileListViewModel.Factory(
      profileRepository = app.profileRepository,
      folderRepository = app.folderRepository,
      proxyRepository = app.proxyRepository,
      preferencesManager = app.preferencesManager
    )
  )

  val proxyManagerViewModel: ProxyManagerViewModel = viewModel(
    factory = ProxyManagerViewModel.Factory(
      proxyRepository = app.proxyRepository
    )
  )

  val settingsViewModel: SettingsViewModel = viewModel(
    factory = SettingsViewModel.Factory(
      preferencesManager = app.preferencesManager,
      profileRepository = app.profileRepository
    )
  )

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Hide bottom bar in browser session or extended settings
  val showBottomBar = currentRoute in listOf(NavRoutes.PROFILES, NavRoutes.MULTI_WEBVIEW, NavRoutes.PROXY_MANAGER, NavRoutes.SETTINGS)

  // Track currently active profile for browser session
  val uiState by profileListViewModel.uiState.collectAsState()
  val activeRunningProfile = uiState.profiles.find { it.isRunning }

  Scaffold(
    bottomBar = {
      AnimatedVisibility(
        visible = showBottomBar,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
      ) {
        Surface(
          shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 8.dp
        ) {
          NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
          ) {
            NavigationBarItem(
              selected = currentRoute == NavRoutes.PROFILES,
              onClick = {
                navController.navigate(NavRoutes.PROFILES) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(Icons.Default.Security, contentDescription = "Profiles") },
              label = { Text("Profiles", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhantomGreen,
                indicatorColor = PhantomGreen
              ),
              modifier = Modifier.testTag("nav_profiles")
            )

            NavigationBarItem(
              selected = currentRoute == NavRoutes.MULTI_WEBVIEW,
              onClick = {
                navController.navigate(NavRoutes.MULTI_WEBVIEW) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(Icons.Default.Tab, contentDescription = "Multi-View") },
              label = { Text("Multi 4-12", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhantomGreen,
                indicatorColor = PhantomGreen
              ),
              modifier = Modifier.testTag("nav_multi_webview")
            )

            NavigationBarItem(
              selected = currentRoute == NavRoutes.PROXY_MANAGER,
              onClick = {
                navController.navigate(NavRoutes.PROXY_MANAGER) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(Icons.Default.Public, contentDescription = "Proxies") },
              label = { Text("Proxies", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhantomGreen,
                indicatorColor = PhantomGreen
              ),
              modifier = Modifier.testTag("nav_proxies")
            )

            NavigationBarItem(
              selected = currentRoute == NavRoutes.SETTINGS,
              onClick = {
                navController.navigate(NavRoutes.SETTINGS) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
              label = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhantomGreen,
                indicatorColor = PhantomGreen
              ),
              modifier = Modifier.testTag("nav_settings")
            )
          }
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { paddingValues ->
    NavHost(
      navController = navController,
      startDestination = NavRoutes.PROFILES,
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp)
    ) {
      composable(NavRoutes.PROFILES) {
        ProfileListScreen(
          viewModel = profileListViewModel,
          onLaunchProfile = { profile ->
            navController.navigate(NavRoutes.BROWSER_SESSION)
          },
          onOpenExtendedSettings = { profileId ->
            navController.navigate(NavRoutes.extendedSettings(profileId))
          },
          onNavigateToProxyManager = {
            navController.navigate(NavRoutes.PROXY_MANAGER)
          },
          onNavigateToSettings = {
            navController.navigate(NavRoutes.SETTINGS)
          },
          onNavigateToCreateProfile = {
            navController.navigate(NavRoutes.CREATE_PROFILE)
          }
        )
      }

      composable(NavRoutes.CREATE_PROFILE) {
        val createProfileViewModel: CreateProfileViewModel = viewModel(
          factory = CreateProfileViewModel.Factory(
            profileRepository = app.profileRepository,
            folderRepository = app.folderRepository,
            proxyRepository = app.proxyRepository
          )
        )
        CreateProfileScreen(
          viewModel = createProfileViewModel,
          onNavigateBack = {
            navController.popBackStack()
          },
          onProfileCreated = { createdProfile ->
            navController.popBackStack(NavRoutes.PROFILES, inclusive = false)
          }
        )
      }

      composable(
        route = NavRoutes.EXTENDED_SETTINGS,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })
      ) { backStackEntry ->
        val profileId = backStackEntry.arguments?.getString("profileId") ?: "new"
        val extendedViewModel: ExtendedSettingsViewModel = viewModel(
          factory = ExtendedSettingsViewModel.Factory(
            profileId = profileId,
            profileRepository = app.profileRepository,
            folderRepository = app.folderRepository,
            proxyRepository = app.proxyRepository
          )
        )
        ExtendedSettingsScreen(
          viewModel = extendedViewModel,
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      composable(NavRoutes.MULTI_WEBVIEW) {
        val proxyState by proxyManagerViewModel.uiState.collectAsState()
        MultiWebViewGridScreen(
          availableProxies = proxyState.proxies
        )
      }

      composable(NavRoutes.PROXY_MANAGER) {
        ProxyManagerScreen(viewModel = proxyManagerViewModel)
      }

      composable(NavRoutes.SETTINGS) {
        SettingsScreen(viewModel = settingsViewModel)
      }

      composable(NavRoutes.BROWSER_SESSION) {
        if (activeRunningProfile != null) {
          BrowserSessionScreen(
            profile = activeRunningProfile,
            onCloseSession = { dataUsed ->
              profileListViewModel.stopProfile(activeRunningProfile.id)
              app.applicationScope.launch(Dispatchers.IO) {
                app.profileRepository.addDataUsage(activeRunningProfile.id, dataUsed)
              }
              navController.popBackStack(NavRoutes.PROFILES, inclusive = false)
            }
          )
        } else {
          // Fallback if none running
          ProfileListScreen(
            viewModel = profileListViewModel,
            onLaunchProfile = { navController.navigate(NavRoutes.BROWSER_SESSION) },
            onOpenExtendedSettings = { id -> navController.navigate(NavRoutes.extendedSettings(id)) },
            onNavigateToProxyManager = { navController.navigate(NavRoutes.PROXY_MANAGER) },
            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
            onNavigateToCreateProfile = { navController.navigate(NavRoutes.CREATE_PROFILE) }
          )
        }
      }
    }
  }
}

