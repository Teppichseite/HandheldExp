package com.handheld.exp.models

class ButtonItem(
    label: String,
    key: String,
    val onClick: () -> Unit
) : Item(label, key)