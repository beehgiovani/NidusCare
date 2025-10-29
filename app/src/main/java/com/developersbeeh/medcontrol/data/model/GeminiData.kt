package com.developersbeeh.medcontrol.data.model


data class GeminiRequest(
    val prompt: String
)


data class GeminiResponse(
    val analysis: String?,
    val error: String?
)