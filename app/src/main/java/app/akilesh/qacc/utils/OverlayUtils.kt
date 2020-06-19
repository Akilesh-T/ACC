package app.akilesh.qacc.utils

import android.os.Build
import app.akilesh.qacc.Const.prefix
import com.topjohnwu.superuser.Shell

object OverlayUtils {

    fun isOverlayEnabled(pkgName: String): Boolean {
        var isEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isEnabled = Shell.su("cmd overlay dump isenabled $pkgName").exec().out.component1() == "true"
        else {
            val overlays = Shell.su("cmd overlay list").exec().out
            overlays.forEach {
                if (it.startsWith("[x]") && it.contains(pkgName))
                    isEnabled = true
            }
        }
        return isEnabled
    }

    fun enableAccent(packageName: String) {
        getInstalledOverlays().forEach{ accent ->
            if (isOverlayEnabled(accent)) disableAccent(accent)
        }
        Shell.su(
            "cmd overlay enable $packageName",
            "cmd overlay set-priority $packageName highest"
        ).submit()
        //Shell.su("cmd overlay enable-exclusive --category ${current.pkgName}").exec()
    }

    fun disableAccent(packageName: String) {
        Shell.su("cmd overlay disable $packageName").exec()
    }

    fun getInstalledOverlays(): MutableSet<String> {
        val installedAccents: MutableList<String> =  Shell.su(
            "pm list packages -f $prefix | sed s/package://"
        ).exec().out

        val installed = mutableSetOf<String>()
        if (installedAccents.isNotEmpty())
            installed.addAll(
                installedAccents.map { it.substringAfterLast('=') }
            )
        return installed
    }
}
