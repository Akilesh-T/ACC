package app.akilesh.qacc.ui.backup

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import android.widget.Toast
import androidx.core.widget.TextViewCompat
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
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.BackupRestoreFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.BackupFile
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.colorpicker.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.SwipeToDelete
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.createBackup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File
import java.io.FileInputStream

class BackupRestoreFragment: Fragment() {

    private lateinit var binding: BackupRestoreFragmentBinding
    private lateinit var viewModel: BackupRestoreViewModel
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

        binding.newBackup.setOnClickListener {
            if (createBackup(requireContext(), false)) {
                Toast.makeText(requireContext(), getString(R.string.backup_created), Toast.LENGTH_SHORT)
                    .show()
                viewModel.backupFiles.value = getBackupFiles()
            }
        }
        binding.restore.setOnClickListener { selectBackupFile() }
        tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)

        val adapter = BackupListAdapter(
            requireContext(), getBackupFiles(), { accents ->
                val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
                val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
                dialogTitleBinding.apply {
                    titleText.text = String.format(resources.getString(R.string.backup_contents))
                    titleIcon.setImageResource(R.drawable.ic_backup_contents)
                }

                val colorListAdapter =
                    ColorListAdapter(
                        requireContext(),
                        accents
                    ) {}

                colorPreviewBinding.recyclerViewColor.apply {
                    adapter = colorListAdapter
                    colorPreviewBinding.recyclerViewColor.layoutManager =
                        GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                    if (useSystemAccent) {
                        edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                            override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                                return EdgeEffect(view.context).apply {
                                    color = context.getColorAccent()
                                }
                            }
                        }
                    }
                }

                MaterialAlertDialogBuilder(requireContext())
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

        viewModel = ViewModelProvider(this).get(BackupRestoreViewModel::class.java)
        viewModel.backupFiles.observe(viewLifecycleOwner, Observer { files ->
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
            TextViewCompat.setCompoundDrawableTintList(newBackupText, colorStateList)
            TextViewCompat.setCompoundDrawableTintList(restoreText, colorStateList)
            localBackupTitle.setTextColor(colorAccent)
        }
    }

    private fun getBackupContents(file: String): MutableList<String> {
        val contents = Shell.su(
            ".$busyBox tar t -f $backupFolder/$file"
        ).exec().out
        contents.removeIf { it == "./" }
        contents.replaceAll { s -> s.removePrefix("./hex").removePrefix("_").removeSuffix(".apk").substringBefore('_') }
        Log.d(file, contents.toString())
        return contents
    }

    private fun getBackupFiles(): MutableList<BackupFile> {
        val backupFiles = mutableListOf<BackupFile>()
        SuFile(backupFolder).walk().forEach { file ->
            if (file.isFile) {
                val contents = getBackupContents(file.name)
                backupFiles.add(
                    BackupFile(
                        file.name,
                        Formatter.formatShortFileSize(requireContext(), file.length()),
                        contents.size,
                        contents.map { Colour("#$it", getString(R.string.hex_code)) }
                    )
                )
            }
        }
        return backupFiles
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
                        requireContext().contentResolver.openFileDescriptor(
                            it,
                            "r"
                        )
                    }
                val backupFile = File(requireContext().filesDir, "acc.tar.gz")
                parcelFileDescriptor?.let {
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    inputStream.use { stream ->
                        SuFileOutputStream(backupFile.absolutePath).use {
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

    private fun getAppList(path: String): Array<SuFile>? {
        Shell.su(
            ".$busyBox tar x -zv -f $path -C ${tempFolder.absolutePath}"
        ).exec()
        val folder = SuFile(tempFolder.absolutePath)
        return folder.listFiles { file ->
            file.length() > 0 && file.extension == "apk" && file.name.startsWith("hex")
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
                insertToDB(getAppList(backupFile.absolutePath)?.map { it.absolutePath })
                backupFile.delete()
                Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                showSnackbar(requireView(), getString(R.string.accents_restored))
            }
        }
        else {
            Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
            tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)
            val apps = getAppList(backupFile.absolutePath)
            apps?.forEach {
                it.setReadable(true, false)
            }
            val filesList = apps?.map { it.absolutePath }?.toTypedArray()
            Log.d("files-list", apps?.contentToString().toString())
            if (filesList != null && filesList.isNotEmpty()) {
                viewModel.restore(filesList)
                viewModel.restoreWorkerId?.let { uuid ->
                    viewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(viewLifecycleOwner, Observer { workInfo ->
                        Log.d("id", workInfo.id.toString())
                        Log.d("state", workInfo.state.name)
                        if (workInfo != null && workInfo.state.isFinished && workInfo.state == WorkInfo.State.SUCCEEDED) {
                            backupFile.delete()
                            showSnackbar(requireView(), getString(R.string.accents_restored))
                            //Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                        }
                        if (workInfo.state == WorkInfo.State.FAILED)
                            Toast.makeText(requireContext(), getString(R.string.error_restoring), Toast.LENGTH_LONG).show()
                    })
                }
            }
            else Toast.makeText(requireContext(), getString(R.string.empty_backup), Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertToDB(appList: List<String>?) {
        val packageManager = requireContext().packageManager
        appList?.forEach {
            val packageInfo = packageManager.getPackageArchiveInfo(it, 0)!!
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = it
            applicationInfo.publicSourceDir = it
            val accentName = packageManager.getApplicationLabel(applicationInfo).toString()
            val pkgName = packageInfo.packageName.toString()
            val resources = packageManager.getResourcesForApplication(applicationInfo)
            val accentLightId = resources.getIdentifier("accent_device_default_light", "color", pkgName)
            val accentDarkId = resources.getIdentifier("accent_device_default_dark", "color", pkgName)
            if (accentLightId != 0 && accentDarkId != 0) {
                val colorLight = resources.getColor(accentLightId, null)
                val colorDark = resources.getColor(accentDarkId, null)
                val accent = Accent(pkgName, accentName, toHex(colorLight), toHex(colorDark))
                val accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)
                accentViewModel.insert(accent)
            }
        }
    }
}