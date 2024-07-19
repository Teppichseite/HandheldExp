package com.handheld.exp.models

class NavigationItem(
    label: String,
    key: String,
    icon: Int = -1,
    path: List<String> = listOf(),
    sortKey: String = "",
    disabled: Boolean = false,
    ) : Item(label, key, icon, path, sortKey, disabled)