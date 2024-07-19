package com.handheld.exp.models

abstract class Item(
    var label: String,
    val key: String,
    val icon: Int = -1,
    val path: List<String> = listOf(),
    var sortKey: String = "",
    var disabled: Boolean = false
)