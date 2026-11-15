package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyPair

private val Context.cubeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cubetuner_prefs"
)

data class SavedConnectionPrefs(
    val ipAddress: String = "",
    val port: Int = 5555,
    val pinnedPackages: Set<String> = emptySet()
)

class CubePreferencesRepository(private val context: Context) {

    private object Keys {
        val LAST_IP = stringPreferencesKey("last_projector_ip")
        val LAST_PORT = intPreferencesKey("last_projector_port")
        val RSA_PUB = stringPreferencesKey("adb_rsa_pub_x509")
        val RSA_PRIV = stringPreferencesKey("adb_rsa_priv_pkcs8")
        val PINNED_APPS = stringSetPreferencesKey("pinned_launcher_packages")
    }

    val prefsFlow: Flow<SavedConnectionPrefs> = context.cubeDataStore.data.map { prefs ->
        SavedConnectionPrefs(
            ipAddress = prefs[Keys.LAST_IP] ?: "",
            port = prefs[Keys.LAST_PORT] ?: 5555,
            pinnedPackages = prefs[Keys.PINNED_APPS] ?: emptySet()
        )
    }

    suspend fun saveConnectionTarget(ip: String, port: Int) {
        context.cubeDataStore.edit { prefs ->
            prefs[Keys.LAST_IP] = ip.trim()
            prefs[Keys.LAST_PORT] = port
        }
    }

    suspend fun togglePinnedPackage(packageName: String) {
        context.cubeDataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_APPS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(packageName)) {
                current.remove(packageName)
            }
            prefs[Keys.PINNED_APPS] = current
        }
    }

    suspend fun getOrCreateAdbKeyPair(): KeyPair {
        val current = context.cubeDataStore.data.first()
        val pub = current[Keys.RSA_PUB]
        val priv = current[Keys.RSA_PRIV]
        if (!pub.isNullOrBlank() && !priv.isNullOrBlank()) {
            AdbCrypto.restoreKeyPair(pub, priv)?.let { return it }
        }
        val generated = AdbCrypto.generateKeyPair()
        context.cubeDataStore.edit { prefs ->
            prefs[Keys.RSA_PUB] = AdbCrypto.encodePublicKeyX509(generated)
            prefs[Keys.RSA_PRIV] = AdbCrypto.encodePrivateKeyPkcs8(generated)
        }
        return generated
    }
}
