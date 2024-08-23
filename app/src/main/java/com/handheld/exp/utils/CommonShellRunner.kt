package com.handheld.exp.utils;
import java.io.BufferedReader
import java.io.InputStreamReader


class CommonShellRunner {
    companion object{

        fun runCommands(vararg commands: String): String {
            return runProcess(Runtime.getRuntime().exec(commandsToShCommand(*commands)))
        }

        fun runAdbCommands(vararg commands: String): String {
            if(!ShizukuUtils.isAvailable()){
                throw Exception("Shizuku is not available")
            }

            return runProcess(ShizukuUtils.exec(commandsToShCommand(*commands)))
        }

        private fun commandsToShCommand(vararg commands: String): Array<String>{
            return arrayOf("sh", "-c", commands.joinToString("\n"))
        }

        // TODO: Perform call async
        private fun runProcess(process: Process): String {
            val stdOutStream = BufferedReader(InputStreamReader(process.inputStream))
            val stdErrStream = BufferedReader(InputStreamReader(process.inputStream))

            val stdOut = stdOutStream.readText().trim()
            val stdErr = stdErrStream.readText().trim()

            stdOutStream.close()
            stdErrStream.close()

            process.waitFor()

            if(process.exitValue() != 0){
                throw Exception(stdErr)
            }

            return stdOut
        }
    }
}