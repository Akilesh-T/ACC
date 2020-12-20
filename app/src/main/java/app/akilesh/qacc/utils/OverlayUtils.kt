package app.akilesh.qacc.utils

import android.content.pm.PackageManager
import app.akilesh.qacc.Const.prefix
import com.topjohnwu.superuser.Shell

object OverlayUtils {

    fun isOverlayEnabled(pkgName: String) = Shell.su("cmd overlay list").exec().out
            .any { it.startsWith("[x]") && it.contains(pkgName) }
        //Shell.su("cmd overlay dump isenabled $pkgName").exec().out.component1() == "true"

    fun enableAccent(packageName: String) {
        getInstalledOverlays().forEach { accent ->
            if (isOverlayEnabled(accent)) disableAccent(accent)
        }
        Shell.su(
            "cmd overlay enable $packageName",
            "cmd overlay set-priority $packageName highest"
        ).exec()
        //Shell.su("cmd overlay enable-exclusive --category ${current.pkgName}").exec()
    }

    fun disableAccent(packageName: String) {
        Shell.su("cmd overlay disable $packageName").exec()
    }

    fun getInstalledOverlays(): MutableList<String> {
        val installedAccents: MutableList<String> =  Shell.su(
            "pm list packages -f $prefix | sed s/package://"
        ).exec().out

        val installed = mutableListOf<String>()
        if (installedAccents.isNotEmpty())
            installed.addAll(
                installedAccents.map { it.substringAfterLast('=') }
            )
        return installed
    }

    fun PackageManager.isOverlayInstalled(pkgName: String): Boolean {
        return try {
            getApplicationInfo(pkgName, 0).enabled
        } catch (e: Exception) {
            false
        }
    }
}
