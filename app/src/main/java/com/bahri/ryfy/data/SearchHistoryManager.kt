package com.bahri.ryfy.data

import android.content.Context
import com.bahri.ryfy.model.Track
import org.json.JSONArray
import org.json.JSONObject

sealed class SearchHistoryEntry {
    data class QueryItem(val id: String, val query: String, val timestamp: Long) : SearchHistoryEntry()
    data class TrackItem(val id: String, val track: Track, val timestamp: Long) : SearchHistoryEntry()
}

object SearchHistoryManager {
    private const val PREFS_NAME = "ryfy_search_history"
    private const val KEY_HISTORY = "history_entries"
    private const val MAX_HISTORY = 15

    fun getHistory(context: Context): List<SearchHistoryEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<SearchHistoryEntry>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val type = obj.optString("type")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                if (type == "query") {
                    val query = obj.optString("query")
                    val id = obj.optString("id", query)
                    if (query.isNotBlank()) {
                        list.add(SearchHistoryEntry.QueryItem(id, query, timestamp))
                    }
                } else if (type == "track") {
                    val trackObj = obj.optJSONObject("track")
                    if (trackObj != null) {
                        val track = Track(
                            id = trackObj.optString("id"),
                            title = trackObj.optString("title"),
                            artist = trackObj.optString("artist"),
                            album = trackObj.optString("album", "RyFy Spotless"),
                            durationMs = trackObj.optLong("durationMs", 210000L),
                            coverUrl = trackObj.optString("coverUrl"),
                            videoId = trackObj.optString("videoId")
                        )
                        list.add(SearchHistoryEntry.TrackItem(track.id, track, timestamp))
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }

    fun addTrack(context: Context, track: Track) {
        val current = getHistory(context).toMutableList()
        current.removeAll {
            when (it) {
                is SearchHistoryEntry.TrackItem -> it.track.id == track.id || (it.track.title.equals(track.title, true) && it.track.artist.equals(track.artist, true))
                else -> false
            }
        }
        current.add(0, SearchHistoryEntry.TrackItem(track.id, track, System.currentTimeMillis()))
        save(context, current.take(MAX_HISTORY))
    }

    fun addQuery(context: Context, query: String) {
        if (query.isBlank()) return
        val current = getHistory(context).toMutableList()
        current.removeAll {
            when (it) {
                is SearchHistoryEntry.QueryItem -> it.query.equals(query.trim(), true)
                else -> false
            }
        }
        current.add(0, SearchHistoryEntry.QueryItem(query.trim(), query.trim(), System.currentTimeMillis()))
        save(context, current.take(MAX_HISTORY))
    }

    fun removeEntry(context: Context, id: String) {
        val current = getHistory(context).toMutableList()
        current.removeAll {
            when (it) {
                is SearchHistoryEntry.QueryItem -> it.id == id
                is SearchHistoryEntry.TrackItem -> it.id == id
            }
        }
        save(context, current)
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(context: Context, list: List<SearchHistoryEntry>) {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            when (item) {
                is SearchHistoryEntry.QueryItem -> {
                    obj.put("type", "query")
                    obj.put("id", item.id)
                    obj.put("query", item.query)
                    obj.put("timestamp", item.timestamp)
                }
                is SearchHistoryEntry.TrackItem -> {
                    obj.put("type", "track")
                    obj.put("id", item.id)
                    obj.put("timestamp", item.timestamp)
                    val tObj = JSONObject().apply {
                        put("id", item.track.id)
                        put("title", item.track.title)
                        put("artist", item.track.artist)
                        put("album", item.track.album)
                        put("durationMs", item.track.durationMs)
                        put("coverUrl", item.track.coverUrl)
                        put("videoId", item.track.videoId)
                    }
                    obj.put("track", tObj)
                }
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, arr.toString())
            .apply()
    }
}
