package com.handheld.exp.models

data class System(
    val name: String? = null,
    val fullName: String? = null,
    val path: String? = null,
    val extension: String? = null,
    val commands: List<Command>? = null,
    val platform: String? = null,
    val theme: String? = null
)

data class Command(
    val label: String,
    val command: String
)
