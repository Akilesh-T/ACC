package app.akilesh.qacc.utils.updates

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val NAME = "arbitrary"
private const val PI_INSTALL = 4837


class InstallApkViewModel(app: Application) : AndroidViewModel(app) {
    private val installer = app.packageManager.packageInstaller
    private val resolver = app.contentResolver

    fun install(apkUri: Uri) {
        viewModelScope.launch(Dispatchers.Main) {
            installUpdate(apkUri)
        }
    }

    private suspend fun installUpdate(apkUri: Uri) =
        withContext(Dispatchers.IO) {
            resolver.openInputStream(apkUri)?.use { apkStream ->
                val length =
                    DocumentFile.fromSingleUri(getApplication(), apkUri)?.length() ?: -1
                val params =
                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                val sessionId = installer.createSession(params)
                val session = installer.openSession(sessionId)

                session.openWrite(NAME, 0, length).use { sessionStream ->
                    apkStream.copyTo(sessionStream)
                    session.fsync(sessionStream)
                }

                val intent = Intent(getApplication(), InstallReceiver::class.java)
                val pi = PendingIntent.getBroadcast(
                    getApplication(),
                    PI_INSTALL,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                session.commit(pi.intentSender)
                session.close()
            }
        }
}
