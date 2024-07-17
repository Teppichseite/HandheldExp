package com.handheld.exp.models

data class GameContext(
    val esDeFolderPath: String,
    val game: Game,
    val system: System,
    val gameMedia: GameMedia
)
