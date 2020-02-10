package app.akilesh.qacc.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.modPath
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.BackupRestoreFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.adapter.BackupListAdapter
import app.akilesh.qacc.ui.adapter.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.SwipeToDelete
import app.akilesh.qacc.viewmodel.AccentViewModel
import app.akilesh.qacc.viewmodel.BackupFileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileInputStream
import java.util.*

class BackupRestoreFragment: Fragment() {

    private lateinit var binding: BackupRestoreFragmentBinding
    private lateinit var model: BackupFileViewModel
    private val busyBox = "/data/adb/magisk/busybox"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BackupRestoreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newBackup.setOnClickListener { createBackup() }
        binding.restore.setOnClickListener { selectBackupFile() }

        val adapter = BackupListAdapter(
            context!!, getBackupFiles(), { file ->
                val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
                val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
                dialogTitleBinding.titleText.text = String.format(resources.getString(R.string.backup_contents))
                dialogTitleBinding.titleIcon.setImageResource(R.drawable.ic_backup_contents)

                val contents = getBackupContents(file)
                contents.removeIf { it == "./" }
                contents.replaceAll { s -> s.removePrefix("./hex").removePrefix("_").removeSuffix(".apk").substringBefore('_') }
                Log.d("contents", contents.toString())

                val accents: List<Colour> = contents.map { Colour("#$it", getString(R.string.hex_code)) }
                val adapter = ColorListAdapter(context!!, accents) {}

                colorPreviewBinding.recyclerViewColor.adapter = adapter
                colorPreviewBinding.recyclerViewColor.layoutManager = LinearLayoutManager(context)

                MaterialAlertDialogBuilder(context)
                    .setCustomTitle(dialogTitleBinding.root)
                    .setView(colorPreviewBinding.root)
                    .create()
                    .show()
            },
            {
                val backupFile = File(backupFolder, it)
                restore(backupFile)
            }
        )

        binding.recyclerViewBackupFiles.adapter = adapter
        binding.recyclerViewBackupFiles.layoutManager = LinearLayoutManager(context)

        model = ViewModelProvider(this).get(BackupFileViewModel::class.java)
        model.backupFiles.observe(viewLifecycleOwner, Observer { files ->
            files.let { adapter.setFiles(it) }
        })

        val swipeToDelete = object : SwipeToDelete(context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val file = adapter.getFileAndRemoveAt(viewHolder.adapterPosition)
                val result = Shell.su(
                    "rm -f $backupFolder/$file"
                ).exec()
                if (result.isSuccess) Toast.makeText(context, getString(R.string.backup_deleted), Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDelete)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewBackupFiles)
    }

    private fun getBackupContents(file: String): MutableList<String> {
        return Shell.su(
            ".$busyBox tar t -f $backupFolder/$file"
        ).exec().out
    }

    private fun getBackupFiles(): MutableList<String> {
        return Shell.su(
            "ls $backupFolder"
        ).exec().out
    }

    private fun createBackup() {

        Shell.su("mkdir -p $backupFolder").exec()
        if (SDK_INT >= P) {
            compress(overlayPath)
        }
        else {
            val overlays = Shell.su(
                "pm list packages -f ${prefix}hex | sed s/package://"
            ).exec().out
            context!!.cacheDir.deleteRecursively()
            overlays.forEach {
                val path = it.substringBeforeLast('=')
                val pkgName = it.substringAfterLast('=')
                val apkName = pkgName.substringAfter(prefix)
                Shell.su(
                    "cp -f $path ${context!!.cacheDir.absolutePath}/$apkName.apk"
                ).exec()
            }
            compress(context!!.cacheDir.absolutePath)
        }
    }

    private fun compress(path: String) {
        var date = Calendar.getInstance().time.toString()
        date = date.replace("\\s".toRegex(), "-")
        val result = Shell.su(
            ".$busyBox tar c -zv -f $backupFolder/$date.tar.gz -C $path ."
        ).exec()
        Log.d("compress", result.out.toString())
        if (result.isSuccess) {
            Toast.makeText(context, getString(R.string.backup_created), Toast.LENGTH_SHORT)
                .show()
            model.backupFiles.value = getBackupFiles()
        }
    }

    private fun selectBackupFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/gzip"
        startActivityForResult(intent, 3)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            val selectedUri = data.data
            val parcelFileDescriptor = selectedUri?.let { context!!.contentResolver.openFileDescriptor(it, "r", null) }
            val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
            val backupFile = File(context!!.cacheDir, "acc.tar.gz")
            inputStream.use { stream ->
                backupFile.outputStream().use {
                    stream.copyTo(it)
                }
            }
            restore(backupFile)
        }
    }

    private fun getAppList(path: String): Array<File>? {
        Shell.su(
            ".$busyBox tar x -zv -f $path -C ${context!!.cacheDir.absolutePath}"
        ).exec()
        return context!!.cacheDir.listFiles { file ->
            file.length() > 0 && file.extension == "apk" && file.name.startsWith("hex")
        }
    }

    private fun restore(backupFile: File) {
        if (SDK_INT >= P) {
            val result = Shell.su("[ -d $modPath ]").exec()
            if (!result.isSuccess) {
                Shell.su("mkdir -p $overlayPath").exec()
                Shell.su(context!!.resources.openRawResource(R.raw.create_module)).exec()
            }

            val restoreResult = Shell.su(
                ".$busyBox tar x -zv -f ${backupFile.absolutePath} -C $overlayPath"
            ).exec()
            Log.d("restore", restoreResult.out.toString())
            if (restoreResult.isSuccess) {
                insertToDB(getAppList(backupFile.absolutePath))
                context!!.cacheDir.deleteRecursively()
                showSnackbar(this.view!!, getString(R.string.accents_restored))
            }
        }
        else {
            context!!.cacheDir.deleteRecursively()
            val apps = getAppList(backupFile.absolutePath)
            apps?.forEach {
                val result = Shell.su(
                    "chmod 644 ${it.absolutePath}",
                    "pm install -r ${it.absolutePath}"
                ).exec()
                Log.d("pm-install", result.out.toString())
            }
            insertToDB(apps)
            context!!.cacheDir.deleteRecursively()
        }
    }

    private fun insertToDB(appList: Array<File>?) {
        val packageManager = context!!.packageManager
        appList?.forEach {
            val packageInfo = packageManager.getPackageArchiveInfo(it.absolutePath, 0)!!
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = it.absolutePath
            applicationInfo.publicSourceDir = it.absolutePath
            val accentName = packageManager.getApplicationLabel(applicationInfo).toString()
            val pkgName = packageInfo.packageName.toString()
            val resources = packageManager.getResourcesForApplication(applicationInfo)
            val accentLightId = resources.getIdentifier("accent_device_default_light", "color", pkgName)
            val colorLight = resources.getColor(accentLightId, null)
            val accentDarkId = resources.getIdentifier("accent_device_default_dark", "color", pkgName)
            val colorDark = resources.getColor(accentDarkId, null)
            val accent = Accent(pkgName, accentName, toHex(colorLight), toHex(colorDark))
            val accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)
            accentViewModel.insert(accent)
        }
    }
}