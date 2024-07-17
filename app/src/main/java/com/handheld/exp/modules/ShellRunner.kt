package com.handheld.exp.modules

interface ShellRunner {

    fun runCommand(command: String): String

    fun runCommands(commands: List<String>): String

}