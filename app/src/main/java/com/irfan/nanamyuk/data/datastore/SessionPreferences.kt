package com.irfan.nanamyuk.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    fun getToken(): Flow<SessionModel> {
        return dataStore.data.map { preferences ->
            SessionModel(
                preferences[TOKEN] ?: "",
                preferences[NAME] ?: "",
                preferences[ID] ?: "",
                preferences[LAT] ?: "",
                preferences[LON] ?: "",
                preferences[TEMP] ?: "",
            )
        }
    }

    suspend fun login(token: String, name: String, id: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[NAME] = name
            preferences[ID] = id
        }
    }

    suspend fun postLatLon(lat: String, lon: String) {
        dataStore.edit { preferences ->
            preferences[LAT] = lat
            preferences[LON] = lon
        }
    }

    suspend fun postTemperature(temperature: String) {
        dataStore.edit { preferences ->
            preferences[TEMP] = temperature
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionPreferences? = null

        private val TOKEN = stringPreferencesKey("token")
        private val NAME = stringPreferencesKey("name")
        private val ID = stringPreferencesKey("id")
        private val LAT = stringPreferencesKey("lat")
        private val LON = stringPreferencesKey("lon")
        private val TEMP = stringPreferencesKey("temperature")

        fun getInstance(dataStore: DataStore<Preferences>): SessionPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}