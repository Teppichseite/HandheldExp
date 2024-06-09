package com.handheld.exp.models

data class Game(
    val path: String? = null,
    val name: String? = null,
    val sortName: String? = null,
    val collectionSortName: String? = null,
    val desc: String? = null,
    val rating: Float? = null,
    val releaseDate: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val players: Int? = null,
    val favorite: Boolean? = null,
    val completed: Boolean? = null,
    val kidGame: Boolean? = null,
    val hidden: Boolean? = null,
    val broken: Boolean? = null,
    val noGameCount: Boolean? = null,
    val noMultiScrape: Boolean? = null,
    val hideMetadata: Boolean? = null,
    val playCount: Int? = null,
    val controller: String? = null,
    val altEmulator: String? = null,
    val lastPlayed: String? = null
)