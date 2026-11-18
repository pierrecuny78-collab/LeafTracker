package com.leafvibe.leaftracker.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinManager(context: Context) {
    private val prefs = context.getSharedPreferences("leaftracker_security", Context.MODE_PRIVATE)
    fun isConfigured() = prefs.contains("pin_hash") && prefs.contains("pin_salt")
    fun setPin(pin: CharArray) {
        require(pin.size in 4..8 && pin.all { it.isDigit() })
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(pin, salt)
        prefs.edit().putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .putInt("failed_attempts", 0).putLong("lock_until", 0L).apply()
    }
    fun verify(pin: CharArray): Boolean {
        val salt = prefs.getString("pin_salt", null) ?: return false
        val hash = prefs.getString("pin_hash", null) ?: return false
        return MessageDigest.isEqual(Base64.decode(hash, Base64.NO_WRAP), derive(pin, Base64.decode(salt, Base64.NO_WRAP)))
    }
    fun isLocked() = System.currentTimeMillis() < prefs.getLong("lock_until", 0L)
    fun remainingLockMillis() = (prefs.getLong("lock_until", 0L) - System.currentTimeMillis()).coerceAtLeast(0L)
    fun registerFailure() {
        val attempts = prefs.getInt("failed_attempts", 0) + 1
        val edit = prefs.edit().putInt("failed_attempts", attempts)
        if (attempts >= 5) edit.putLong("lock_until", System.currentTimeMillis() + 30_000L * (1L shl (attempts - 5).coerceAtMost(6)))
        edit.apply()
    }
    fun registerSuccess() { prefs.edit().putInt("failed_attempts", 0).putLong("lock_until", 0L).apply() }
    private fun derive(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin, salt, 210_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
}