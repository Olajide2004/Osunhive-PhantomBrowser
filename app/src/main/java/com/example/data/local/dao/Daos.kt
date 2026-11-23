package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FingerprintPresetEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProxyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles ORDER BY updatedAt DESC")
  fun getAllProfiles(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM profiles WHERE id = :id")
  suspend fun getProfileById(id: String): ProfileEntity?

  @Query("SELECT * FROM profiles WHERE folderId = :folderId ORDER BY updatedAt DESC")
  fun getProfilesByFolder(folderId: String): Flow<List<ProfileEntity>>

  @Query("SELECT COUNT(*) FROM profiles")
  fun getProfilesCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: ProfileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfiles(profiles: List<ProfileEntity>)

  @Update
  suspend fun updateProfile(profile: ProfileEntity)

  @Query("UPDATE profiles SET isRunning = :isRunning WHERE id = :id")
  suspend fun updateRunningState(id: String, isRunning: Boolean)

  @Query("UPDATE profiles SET isRunning = 0")
  suspend fun stopAllProfiles()

  @Query("UPDATE profiles SET dataUsedMb = dataUsedMb + :deltaMb, updatedAt = :timestamp WHERE id = :id")
  suspend fun addDataUsed(id: String, deltaMb: Double, timestamp: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteProfile(profile: ProfileEntity)

  @Query("DELETE FROM profiles WHERE id = :id")
  suspend fun deleteProfileById(id: String)
}

@Dao
interface ProxyDao {
  @Query("SELECT * FROM proxies ORDER BY isFavorite DESC, createdAt DESC")
  fun getAllProxies(): Flow<List<ProxyEntity>>

  @Query("SELECT * FROM proxies WHERE id = :id")
  suspend fun getProxyById(id: String): ProxyEntity?

  @Query("SELECT * FROM proxies WHERE isRotating = 1 ORDER BY isFavorite DESC, countryName ASC")
  fun getRotatingProxies(): Flow<List<ProxyEntity>>

  @Query("SELECT * FROM proxies WHERE isRotating = 0 ORDER BY createdAt DESC")
  fun getCustomProxies(): Flow<List<ProxyEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProxy(proxy: ProxyEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProxies(proxies: List<ProxyEntity>)

  @Update
  suspend fun updateProxy(proxy: ProxyEntity)

  @Delete
  suspend fun deleteProxy(proxy: ProxyEntity)

  @Query("DELETE FROM proxies WHERE id = :id")
  suspend fun deleteProxyById(id: String)

  @Query("UPDATE proxies SET latencyMs = :latency, lastStatus = :status, lastTestedTime = :timestamp WHERE id = :id")
  suspend fun updateLatency(id: String, latency: Long, status: String, timestamp: Long)

  @Query("UPDATE proxies SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun updateFavorite(id: String, isFavorite: Boolean)
}

@Dao
interface FolderDao {
  @Query("SELECT * FROM folders ORDER BY orderIndex ASC, name ASC")
  fun getAllFolders(): Flow<List<FolderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolder(folder: FolderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolders(folders: List<FolderEntity>)

  @Delete
  suspend fun deleteFolder(folder: FolderEntity)

  @Query("DELETE FROM folders WHERE id = :id")
  suspend fun deleteFolderById(id: String)
}

@Dao
interface FingerprintPresetDao {
  @Query("SELECT * FROM fingerprint_presets ORDER BY os ASC, title ASC")
  fun getAllPresets(): Flow<List<FingerprintPresetEntity>>

  @Query("SELECT * FROM fingerprint_presets WHERE os = :os")
  suspend fun getPresetsByOs(os: String): List<FingerprintPresetEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPresets(presets: List<FingerprintPresetEntity>)
}
