package com.roomanalyzer.roomscore.data

data class HomeAssistantConfig(
    val url: String = "",
    val token: String = "",
    val isConfigured: Boolean = false
)
