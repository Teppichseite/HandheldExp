package com.handheld.exp.utils;
import java.io.BufferedReader
import java.io.InputStreamReader


class CommonShellRunner {
    companion object{
        fun runCommands(vararg commands: String): String {
            return runProcess(Runtime.getRuntime().exec(commandsToShCommand(*commands)))
        }

        fun commandsToShCommand(vararg commands: String): Array<String>{
            return arrayOf("sh", "-c", commands.joinToString("\n"))
        }

        fun runProcess(process: Process): String {
            val stdOutStream = BufferedReader(InputStreamReader(process.inputStream))
            val stdErrStream = BufferedReader(InputStreamReader(process.inputStream))

            val stdOut = stdOutStream.readText().trim()
            val stdErr = stdErrStream.readText().trim()

            stdOutStream.close()
            stdErrStream.close()

            process.waitFor()

            return "$stdOut$stdErr"
        }
    }
}