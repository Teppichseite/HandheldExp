package com.handheld.exp.utils;

import com.handheld.exp.modules.ShellRunner

class BasicShellRunner : ShellRunner {
    override fun runCommand(command: String): String {
        try {
            /*val process = Runtime.getRuntime()
                .exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine()
                    .also { line = it } != null
            ) {
                output.append(line)
                    .append("\n")
            }
            process.waitFor()
            reader.close()*/

            val process = Runtime.getRuntime()
                .exec(command)
            return ""
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error executing command: ${e.message}"
        }
    }

    override fun runCommands(commands: List<String>): String {
        val commandStr = commands.joinToString(" && ")
        return runCommand(commandStr)
    }
}