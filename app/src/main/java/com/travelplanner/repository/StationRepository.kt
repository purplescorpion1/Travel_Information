package com.travelplanner.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.travelplanner.model.Station
import java.io.InputStreamReader

class StationRepository(
    private val context: Context?,
    private val injectedStations: List<Station>? = null
) {
    // Secondary constructor for standard Android usage
    constructor(context: Context) : this(context, null)

    private val stations: List<Station> by lazy {
        if (injectedStations != null) {
            injectedStations
        } else if (context != null) {
            try {
                val inputStream = context.assets.open("stations.json")
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<Station>>() {}.type
                Gson().fromJson<List<Station>>(reader, type) ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun getAllStations(): List<Station> = stations

    fun getSuggestions(query: String): List<Station> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        // 1. Matches on CRS code (3 letters)
        val crsMatches = stations.filter { it.crs.lowercase() == q }
        if (crsMatches.isNotEmpty()) return crsMatches

        // 2. Starts with name match
        val prefixMatches = stations.filter { it.name.lowercase().startsWith(q) }
            .sortedBy { it.name.length }

        // 3. Contains name match (excluding those already in prefix matches)
        val containsMatches = stations.filter {
            it.name.lowercase().contains(q) && !it.name.lowercase().startsWith(q)
        }.sortedBy { it.name.length }

        return (prefixMatches + containsMatches).take(10)
    }

    fun getClosestMatch(query: String): Station? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null

        // 1. Exact or prefix match
        val exact = stations.firstOrNull { it.name.lowercase() == q || it.crs.lowercase() == q }
        if (exact != null) return exact

        val startsWith = stations.firstOrNull { it.name.lowercase().startsWith(q) }
        if (startsWith != null) return startsWith

        // 2. Fuzzy match based on Levenshtein distance
        var bestStation: Station? = null
        var minDistance = Int.MAX_VALUE

        for (station in stations) {
            val nameLower = station.name.lowercase()
            var localMin = Int.MAX_VALUE

            // Distance to full name
            localMin = minOf(localMin, levenshteinDistance(q, nameLower))

            // Distance to prefix of the name of same length
            if (nameLower.length >= q.length) {
                val prefix = nameLower.substring(0, q.length)
                localMin = minOf(localMin, levenshteinDistance(q, prefix))
            }

            // Distance to individual words or their prefixes
            val words = nameLower.split(" ", "-", "&", "/")
            for (word in words) {
                if (word.isNotEmpty()) {
                    localMin = minOf(localMin, levenshteinDistance(q, word))
                    if (word.length >= q.length) {
                        val wordPrefix = word.substring(0, q.length)
                        localMin = minOf(localMin, levenshteinDistance(q, wordPrefix))
                    }
                }
            }

            if (localMin < minDistance) {
                minDistance = localMin
                bestStation = station
            }
        }

        return bestStation
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                if (s1[i - 1] == s2[j - 1]) {
                    dp[j] = prev
                } else {
                    dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + 1)
                }
                prev = temp
            }
        }
        return dp[s2.length]
    }
}
