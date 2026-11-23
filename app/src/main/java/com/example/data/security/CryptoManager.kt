package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
  private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  private const val KEY_ALIAS = "PhantomProxyKeyAlias"
  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val IV_LENGTH = 12
  private const val TAG_LENGTH = 128

  private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
    load(null)
  }

  private fun getOrCreateSecretKey(): SecretKey {
    if (keyStore.containsAlias(KEY_ALIAS)) {
      return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    val spec = KeyGenParameterSpec.Builder(
      KEY_ALIAS,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .build()

    keyGenerator.init(spec)
    return keyGenerator.generateKey()
  }

  fun encrypt(plainText: String): String {
    if (plainText.isEmpty()) return ""
    return try {
      val secretKey = getOrCreateSecretKey()
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val iv = cipher.iv
      val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
      val combined = ByteArray(iv.size + cipherBytes.size)
      System.arraycopy(iv, 0, combined, 0, iv.size)
      System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
      Base64.encodeToString(combined, Base64.NO_WRAP)
    } catch (e: Exception) {
      // Fallback base64
      Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
  }

  fun decrypt(cipherText: String): String {
    if (cipherText.isEmpty()) return ""
    return try {
      val combined = Base64.decode(cipherText, Base64.NO_WRAP)
      if (combined.size <= IV_LENGTH) return ""
      val iv = combined.copyOfRange(0, IV_LENGTH)
      val cipherBytes = combined.copyOfRange(IV_LENGTH, combined.size)
      val secretKey = getOrCreateSecretKey()
      val cipher = Cipher.getInstance(TRANSFORMATION)
      val spec = GCMParameterSpec(TAG_LENGTH, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
      val plainBytes = cipher.doFinal(cipherBytes)
      String(plainBytes, Charsets.UTF_8)
    } catch (e: Exception) {
      try {
        String(Base64.decode(cipherText, Base64.NO_WRAP), Charsets.UTF_8)
      } catch (_: Exception) {
        ""
      }
    }
  }
}
