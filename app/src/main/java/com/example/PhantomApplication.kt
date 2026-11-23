package com.example

import android.app.Application
import com.example.data.local.PhantomDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.FolderRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.ProxyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhantomApplication : Application() {
  val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  val database by lazy { PhantomDatabase.getDatabase(this, applicationScope) }
  val preferencesManager by lazy { PreferencesManager(this) }

  val profileRepository by lazy {
    ProfileRepository(database.profileDao(), database.fingerprintPresetDao())
  }

  val proxyRepository by lazy {
    ProxyRepository(database.proxyDao())
  }

  val folderRepository by lazy {
    FolderRepository(database.folderDao())
  }

  override fun onCreate() {
    super.onCreate()
    // Make sure all profiles are stopped on app launch
    applicationScope.launch(Dispatchers.IO) {
      profileRepository.stopAllProfiles()
    }
  }
}
