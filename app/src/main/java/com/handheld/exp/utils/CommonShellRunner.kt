package com.handheld.exp.utils;

class CommonShellRunner {

    fun runCommand(command: String): String {
        // TODO: Handle command execution result and waiting properly
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        return ""
    }

    fun runCommands(commands: List<String>): String {
        for(command in commands){
            runCommand(command)
        }
        return ""
    }

}