package me.maxid.linklater

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Creating the DataStore instance
private val Context.dataStore by preferencesDataStore(name = "list_datastore")

class ListDataStore(private val context: Context) {
    private val gson = Gson()

    // Key for storing the list
    private val listKey = stringPreferencesKey("saved_list")

    /**
     * Save a list of strings to DataStore
     */
    suspend fun saveList(list: List<Pair<String, String>>) {
        // Serialize the list as JSON and save it directly
        context.dataStore.edit { preferences ->
            preferences[listKey] = gson.toJson(list)
        }
    }

    /**
     * Retrieve a list of strings from DataStore
     */
    fun getList(): Flow<List<Pair<String, String>>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[listKey] ?: "[]"
            // Deserialize the JSON into a List<Pair<String, String>>
            val type = object : TypeToken<List<Pair<String, String>>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    /**
     * Append a new item to the existing list in DataStore
     */
    suspend fun appendToList(item: String, time: String) {
        // Retrieve the current list
        val currentList = getList().firstOrNull() ?: emptyList()

        // Add the new item as a pair and save the updated list
        saveList(currentList + Pair(item, time))

    }
}