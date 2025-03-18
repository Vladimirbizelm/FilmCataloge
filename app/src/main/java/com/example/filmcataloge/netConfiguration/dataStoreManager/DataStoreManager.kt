package com.example.filmcataloge.netConfiguration.dataStoreManager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.dataStore
import com.example.filmcataloge.uiConfiguration.fragments.FavoritesFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {
    companion object{
        private val REQUEST_TOKEN_KEY = stringPreferencesKey("request_token")
        private val SESSION_ID = stringPreferencesKey("session_id")
    }
    suspend fun saveRequestToken(token: String){
        context.dataStore.edit {
            it[REQUEST_TOKEN_KEY] = token
        }
    }
    suspend fun getRequestToken(): String?{
        return context.dataStore.data.map {
            it[REQUEST_TOKEN_KEY]
        }.first()
    }
    suspend fun deleteRequestToken(){
        context.dataStore.edit {
            it.remove(REQUEST_TOKEN_KEY)
        }
    }
    suspend fun saveSessionId(sessionId: String){
        context.dataStore.edit {
            it[SESSION_ID] = sessionId
        }
    }
    suspend fun getSessionId(): String?{
        return context.dataStore.data.map {
            it[SESSION_ID]
        }.first()
    }
}