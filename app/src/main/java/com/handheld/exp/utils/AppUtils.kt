package com.handheld.exp.utils
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.handheld.exp.models.AppContext
import java.util.Locale

class AppUtils(private val context: Context) {

    fun getCurrentlyOpenedApps(): List<AppContext> {

        // TODO: Replace with proper solution which does not use the dumpsys
        val recentTasksPattern = Regex("""\* Recent #\d*: Task\{.*\n( {4}.*\n)*""")
        val taskIdPattern = Regex("""taskId=(\d+)""")
        val packagePattern = Regex("""mActivityComponent=([\w.:]+)""")
        val typePattern = Regex("""type=(\w+)""")

        val recentTasksString = CommonShellRunner
            .runAdbCommands("dumpsys activity recents")

        val allowedTypes = arrayOf("standard", "home", "recents")

        val contexts = recentTasksPattern.findAll(recentTasksString)
            .map {
                val taskString = it.value

                val taskId = taskIdPattern.find(taskString)?.groupValues?.get(1)
                val packageName = packagePattern.find(taskString)?.groupValues?.get(1)
                val type = typePattern.find(taskString)?.groupValues?.get(1)

                val appInfo = context.packageManager.getApplicationInfo(packageName!!, 0)
                val appName = context.packageManager.getApplicationLabel(appInfo)
                    .toString()

                if(!allowedTypes.contains(type?.trim()?.toLowerCase(Locale.ROOT))){
                    return@map null
                }

                AppContext(
                    taskId!!.toInt(),
                    appName,
                    packageName
                )
            }
            .filterNotNull()
            .toList()

        return contexts
    }

    fun getLaunchAbleApps(): List<AppContext>{
        val packages =
            context.packageManager.getInstalledApplications(PackageManager.PERMISSION_GRANTED)

        return packages
            .filter { isLaunchAblePackage(it) }
            .map {
                val label = context.packageManager.getApplicationLabel(it)
                    .toString()

                AppContext(
                    name = label,
                    packageName = it.packageName,
                    taskId = null
                )
            }
            .sortedBy { it.name }
    }

    private fun isLaunchAblePackage(applicationInfo: ApplicationInfo): Boolean {
        return context.packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null
    }

    fun getCurrentApp(): AppContext {
        return getCurrentlyOpenedApps().first()
    }

    fun removeTask(taskId: Int){
        CommonShellRunner.runAdbCommands("am stack remove $taskId")
    }

    fun stopApp(packageName: String){
        CommonShellRunner.runAdbCommands("am force-stop $packageName")
    }

    fun closeCurrentApp(): AppContext? {
        val currentApp = getCurrentApp()

        if(currentApp.taskId == null){
            return null
        }

        removeTask(currentApp.taskId)
        return currentApp
    }

    fun reopenCurrentApp(): AppContext? {
        val currentApp = closeCurrentApp() ?: return null
        openApp(currentApp.packageName)
        return currentApp
    }

    fun openApp(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
    }

}