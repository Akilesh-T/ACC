package app.akilesh.qacc

import android.app.Application
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.setContext
import app.akilesh.qacc.utils.AppUtils.applyTheme
import app.akilesh.qacc.utils.AppUtils.default
import com.topjohnwu.superuser.Shell

class App: Application() {

    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(true)
        Shell.Config.setTimeout(10)
    }
    
    override fun onCreate() {
        super.onCreate()
        setContext(this)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPreferences.getString("themePref", default)
        applyTheme(theme)
    }

}
