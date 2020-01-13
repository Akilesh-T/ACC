package app.akilesh.qacc

import android.app.Application
import androidx.preference.PreferenceManager
import app.akilesh.qacc.utils.AppUtils
import com.topjohnwu.superuser.Shell

class App: Application() {

    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.setFlags(Shell.FLAG_VERBOSE_LOGGING)
        Shell.Config.verboseLogging(true)
        Shell.Config.setTimeout(10)
    }

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPreferences.getString("themePref", AppUtils.default)
        AppUtils.applyTheme(theme)

    }
}
