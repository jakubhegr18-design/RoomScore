package com.roomanalyzer.roomscore.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class HomeAssistantSensorData(
    val temperature: Float? = null,
    val humidity: Float? = null,
    val illuminance: Float? = null,
    val airQuality: String? = null,
    val co2: Float? = null,
    val pressure: Float? = null,
    val noise: Float? = null,
    val isWindowOpen: Boolean? = null,
    val hasMotion: Boolean? = null
)

class HomeAssistantRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ha_config", Context.MODE_PRIVATE)

    fun loadConfig(): HomeAssistantConfig {
        val url = prefs.getString("ha_url", "") ?: ""
        val token = prefs.getString("ha_token", "") ?: ""
        return HomeAssistantConfig(
            url = url,
            token = token,
            isConfigured = url.isNotBlank() && token.isNotBlank()
        )
    }

    fun saveConfig(config: HomeAssistantConfig) {
        prefs.edit()
            .putString("ha_url", config.url.trimEnd('/'))
            .putString("ha_token", config.token)
            .apply()
    }

    fun clearConfig() {
        prefs.edit().remove("ha_url").remove("ha_token").apply()
    }

    suspend fun testConnection(config: HomeAssistantConfig): Result<String> {
        return fetchJson("${config.url}/api/", config.token)
    }

    private val hacsEndpoint = "/api/roomscore"

    suspend fun fetchSensorData(config: HomeAssistantConfig): HomeAssistantSensorData {
        if (!config.isConfigured) return HomeAssistantSensorData()

        val hacsResult = try {
            val response = fetchJson("${config.url}$hacsEndpoint", config.token)
            response.map { json ->
                parseHacsData(JSONObject(json))
            }.getOrNull()
        } catch (_: Exception) { null }

        if (hacsResult != null) return hacsResult

        return try {
            val response = fetchJson("${config.url}/api/states", config.token)
            response.map { json ->
                parseSensorData(JSONArray(json))
            }.getOrElse { HomeAssistantSensorData() }
        } catch (e: Exception) {
            HomeAssistantSensorData()
        }
    }

    private fun parseHacsData(json: JSONObject): HomeAssistantSensorData {
        return HomeAssistantSensorData(
            temperature = json.optString("temperature", null)?.toFloatOrNull(),
            humidity = json.optString("humidity", null)?.toFloatOrNull(),
            illuminance = json.optString("illuminance", null)?.toFloatOrNull(),
            co2 = json.optString("co2", null)?.toFloatOrNull(),
            pressure = json.optString("pressure", null)?.toFloatOrNull(),
            noise = json.optString("noise", null)?.toFloatOrNull(),
            airQuality = json.optString("air_quality", null),
            isWindowOpen = if (json.has("is_window_open")) json.optBoolean("is_window_open", false) else null,
            hasMotion = if (json.has("has_motion")) json.optBoolean("has_motion", false) else null
        )
    }

    private fun parseSensorData(states: JSONArray): HomeAssistantSensorData {
        var temp: Float? = null
        var hum: Float? = null
        var lux: Float? = null
        var co2: Float? = null
        var press: Float? = null
        var noise: Float? = null
        var airQ: String? = null
        var windowOpen: Boolean? = null
        var motion: Boolean? = null

        for (i in 0 until states.length()) {
            val state = states.getJSONObject(i)
            val entityId = state.optString("entity_id", "")
            val entityState = state.optString("state", "")

            try {
                when {
                    entityId.startsWith("sensor.") && entityId.contains("temperature") ->
                        temp = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && (entityId.contains("humidity") || entityId.contains("moisture")) ->
                        hum = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && (entityId.contains("illuminance") || entityId.contains("lux") || entityId.contains("light")) ->
                        lux = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && entityId.contains("co2") ->
                        co2 = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && entityId.contains("pressure") ->
                        press = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && (entityId.contains("noise") || entityId.contains("sound")) ->
                        noise = entityState.toFloatOrNull()
                    entityId.startsWith("sensor.") && (entityId.contains("air_quality") || entityId.contains("aqi")) ->
                        airQ = entityState
                    entityId.startsWith("binary_sensor.") && entityId.contains("window") ->
                        windowOpen = entityState == "on"
                    entityId.startsWith("binary_sensor.") && entityId.contains("motion") ->
                        motion = entityState == "on"
                }
            } catch (_: Exception) { }
        }

        return HomeAssistantSensorData(temp, hum, lux, airQ, co2, press, noise, windowOpen, motion)
    }

    private suspend fun fetchJson(urlString: String, token: String): Result<String> {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val code = connection.responseCode
            if (code != 200) {
                return Result.failure(Exception("HTTP $code"))
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            connection.disconnect()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
