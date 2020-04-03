package app.akilesh.qacc.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.modPath
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.busyBox
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.BackupRestoreFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.adapter.BackupListAdapter
import app.akilesh.qacc.ui.adapter.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.SwipeToDelete
import app.akilesh.qacc.utils.workers.WorkerUtils.makeStatusNotification
import app.akilesh.qacc.viewmodel.AccentViewModel
import app.akilesh.qacc.viewmodel.BackupFileViewModel
import app.akilesh.qacc.viewmodel.RestoreViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class BackupRestoreFragment: Fragment() {

    private lateinit var binding: BackupRestoreFragmentBinding
    private lateinit var model: BackupFileViewModel
    private lateinit var tempFolder: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BackupRestoreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetWorldReadable")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) setColor(requireContext().getColorAccent())

        binding.newBackup.setOnClickListener { createBackup() }
        binding.restore.setOnClickListener { selectBackupFile() }
        tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)

        val adapter = BackupListAdapter(
            requireContext(), getBackupFiles(), { file ->
                val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
                val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
                dialogTitleBinding.apply {
                    titleText.text = String.format(resources.getString(R.string.backup_contents))
                    titleIcon.setImageResource(R.drawable.ic_backup_contents)
                }

                val contents = getBackupContents(file)
                contents.removeIf { it == "./" }
                contents.replaceAll { s -> s.removePrefix("./hex").removePrefix("_").removeSuffix(".apk").substringBefore('_') }
                Log.d("contents", contents.toString())

                val accents: List<Colour> = contents.map { Colour("#$it", getString(R.string.hex_code)) }
                val colorListAdapter = ColorListAdapter(requireContext(), accents) {}

                colorPreviewBinding.recyclerViewColor.apply {
                    adapter = colorListAdapter
                    colorPreviewBinding.recyclerViewColor.layoutManager =
                        GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                }

                MaterialAlertDialogBuilder(context)
                    .setCustomTitle(dialogTitleBinding.root)
                    .setView(colorPreviewBinding.root)
                    .create()
                    .show()
            },
            {
                val backupFile = File(backupFolder, it)
                val temp = File(requireContext().filesDir, "acc.tar.gz")
                Shell.su("cp -af ${backupFile.absolutePath} ${temp.absolutePath}").exec()
                temp.setReadable(true, false)
                restore(temp)
            }
        )

        binding.recyclerViewBackupFiles.adapter = adapter
        binding.recyclerViewBackupFiles.layoutManager = LinearLayoutManager(context)

        model = ViewModelProvider(this).get(BackupFileViewModel::class.java)
        model.backupFiles.observe(viewLifecycleOwner, Observer { files ->
            files.let { adapter.setFiles(it) }
        })

        val swipeToDelete = object : SwipeToDelete(requireContext()) {
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

    private fun setColor(colorAccent: Int) {
        binding.apply {
            val colorStateList = ColorStateList.valueOf(colorAccent)
            newBackupText.compoundDrawableTintList = colorStateList
            restoreText.compoundDrawableTintList = colorStateList
            localBackupTitle.setTextColor(colorAccent)
            recyclerViewBackupFiles
        }
    }

    private fun getBackupContents(file: String): MutableList<String> {
        return Shell.su(
            ".$busyBox tar t -f $backupFolder/$file"
        ).exec().out
    }

    private fun getBackupFiles(): MutableList<String> {
        return Shell.su(
            "ls -1t $backupFolder"
        ).exec().out
    }

    private fun createBackup() {

        Shell.su("mkdir -p $backupFolder").exec()
        if (SDK_INT >= P) {
            if (Shell.su("[ \"$(ls -A $overlayPath)\" ]").exec().isSuccess)
                compress(overlayPath)
        }
        else {
            val installedAccents: MutableList<String> = Shell.su(
                "pm list packages -f $prefix | sed s/package://"
            ).exec().out

            if (installedAccents.isNotEmpty()) {
                requireContext().cacheDir.deleteRecursively()
                installedAccents.forEach {
                    val path = it.substringBeforeLast('=')
                    val pkgName = it.substringAfterLast('=')
                    val apkName = pkgName.substringAfter(prefix)
                    Shell.su(
                        "cp -f $path ${requireContext().cacheDir.absolutePath}/$apkName.apk"
                    ).exec()
                }
                compress(requireContext().cacheDir.absolutePath)
            }
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/x-gzip", "application/gzip", "application/octet-stream"))
        startActivityForResult(intent, 3)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
                val selectedUri = data.data
                Log.d("selected-uri", selectedUri.toString())
                Log.d("selected-path", selectedUri?.path.toString())
                Log.d("mime-type",
                    selectedUri?.let { requireContext().contentResolver.getType(it) }.toString()
                )
                val parcelFileDescriptor =
                    selectedUri?.let {
                        requireContext().applicationContext.contentResolver.openFileDescriptor(
                            it,
                            "r"
                        )
                    }
                val backupFile = File(requireContext().applicationContext.filesDir, "acc.tar.gz")
                backupFile.setWritable(true)
                parcelFileDescriptor?.let {
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    inputStream.use { stream ->
                        FileOutputStream(backupFile.absolutePath).use {
                            stream.copyTo(it)
                        }
                    }
                }
                parcelFileDescriptor?.close()
                restore(backupFile)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getAppList(path: String): Array<File>? {
        Shell.su(
            ".$busyBox tar x -zv -f $path -C ${tempFolder.absolutePath}"
        ).exec()
        return tempFolder.listFiles { file ->
            file.length() > 0 && file.extension == "apk" && file.name.startsWith("hex")
        }
    }

    private fun getBackupAppsList(path: String): List<String> {
        Shell.su(
            ".$busyBox tar x -zv -f $path -C ${tempFolder.absolutePath}"
        ).exec().apply {
            Log.d("temp-files", code.toString() + "\n" + out +  "\n" + err)
        }
        val list = Shell.su("ls -d -1 -a ${tempFolder.absolutePath}/*").exec().out.apply {
            Log.d("ls", this.toString())
        }
        return list.filter {
            it.endsWith("apk")
        }
    }

    @SuppressLint("SetWorldReadable")
    private fun restore(backupFile: File) {
        Toast.makeText(requireContext(), requireContext().getString(R.string.restoring_accents), Toast.LENGTH_SHORT).show()
        if (SDK_INT >= P) {
            val result = Shell.su("[ -d $modPath ]").exec()
            if (!result.isSuccess) {
                Shell.su("mkdir -p $overlayPath").exec()
                Shell.su(requireContext().resources.openRawResource(R.raw.create_module)).exec()
            }

            val restoreResult = Shell.su(
                ".$busyBox tar x -zv -f ${backupFile.absolutePath} -C $overlayPath"
            ).exec()
            Log.d("restore", restoreResult.out.toString())
            if (restoreResult.isSuccess) {
                insertToDB(getAppList(backupFile.absolutePath))
                backupFile.delete()
                Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                showSnackbar(this.requireView(), getString(R.string.accents_restored))
            }
        }
        else {
            Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
            tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)
            val apps = getBackupAppsList(backupFile.absolutePath)
            apps.forEach {
                File(it).setReadable(true, false)
            }
            val filesList = apps.toTypedArray()
            Log.d("files-list", filesList.toString())
            val restoreViewModel = ViewModelProvider(this).get(RestoreViewModel::class.java)
            if (filesList.isNotEmpty()) {
                makeStatusNotification(requireContext(), true)
                restoreViewModel.restore(filesList)
                restoreViewModel.outputWorkInfo.observe(viewLifecycleOwner, Observer { listOfWorkInfo ->
                    if (listOfWorkInfo.isNullOrEmpty()) {
                        return@Observer
                    }

                    val workInfo = listOfWorkInfo[0]
                    if (workInfo.state.isFinished && workInfo.state == WorkInfo.State.SUCCEEDED) {
                        makeStatusNotification(requireContext(), false)
                        backupFile.delete()
                        Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                    }
                })
            }
            else Toast.makeText(requireContext(), getString(R.string.empty_backup), Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertToDB(appList: Array<File>?) {
        val packageManager = requireContext().packageManager
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