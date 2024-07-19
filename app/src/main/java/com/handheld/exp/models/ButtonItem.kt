package com.handheld.exp.models

class ButtonItem(
    label: String,
    key: String,
    icon: Int = -1,
    path: List<String> = listOf(),
    sortKey: String = "",
    disabled: Boolean = false,
    val onClick: () -> Unit
) : Item(label, key, icon, path, sortKey, disabled)