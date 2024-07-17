package com.handheld.exp.models

class TextItem(
    label: String,
    key: String,
    path: List<String> = listOf(),
    sortKey: String = "",
    disabled: Boolean = false,
) : Item(label, key, path, sortKey, disabled)