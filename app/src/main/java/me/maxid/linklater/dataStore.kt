package me.maxid.linklater

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Creating DataStore instance
private val Context.dataStore by preferencesDataStore(name = "list_datastore")

class ListDataStore(private val context: Context) {
    private val gson = Gson()

    // Key for storing the list
    private val listKey = stringPreferencesKey("saved_list")

    /**
     * Save a list of strings to DataStore
     */
    suspend fun saveList(list: List<String>) {
        // Serialize the list as JSON and save it
        context.dataStore.edit { preferences ->
            preferences[listKey] = gson.toJson(list)
        }
    }

    /**
     * Retrieve a list of strings from DataStore
     */
    fun getList(): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[listKey] ?: "[]"
            // Deserialize the JSON into a list
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    suspend fun appendToList(item: String) {
        // Retrieve the existing list (or create a new one if empty) and append the new item
        val currentList = getList().map { it }.firstOrNull() ?: emptyList()
        saveList(currentList + item)
    }
}