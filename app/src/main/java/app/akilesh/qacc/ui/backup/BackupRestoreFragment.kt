package app.akilesh.qacc.ui.backup

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.colorList
import app.akilesh.qacc.Const.Paths.backupFolder
import app.akilesh.qacc.Const.Paths.busyBox
import app.akilesh.qacc.Const.Paths.modPath
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.FragmentBackupRestoreBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.BackupFile
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.home.accent.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.createBackup
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackBar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.SwipeToDelete
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File
import java.io.FileInputStream

class BackupRestoreFragment: Fragment() {

    private lateinit var binding: FragmentBackupRestoreBinding
    private val viewModel: BackupRestoreViewModel by viewModels()
    private lateinit var tempFolder: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) setColor(requireContext().getColorAccent())

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.updatePadding(
                top = insets.getInsets(systemBars()).top,
                bottom = insets.getInsets(systemBars()).bottom
            )
            insets
        }

        val getContent = registerForActivityResult(OpenDocument()) { uri: Uri? ->
            Log.d("selected-uri", uri.toString())
            uri?.let {
                Log.d("selected-path", uri.path.toString())
                requireContext().contentResolver.getType(uri)?.let { mimeType ->
                    Log.d("mime-type", mimeType)
                }
                if (uri.path.toString().endsWith(".tar.gz")) {
                    val parcelFileDescriptor =
                        uri.let {
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
            }
        }

        binding.restore.setOnClickListener {
            getContent.launch(arrayOf(
                "application/gzip",
                "application/x-gzip",
                "application/octet-stream"
            ))
        }

        tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)

        val navController = findNavController()

        val backupListAdapter = BackupListAdapter(
            requireContext(), getBackupFiles(), { accents ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    colorList,
                    accents
                )
                navController.navigate(R.id.color_picker_dialog)
            },
            {
                val backupFile = File(backupFolder, it)
                val temp = File(requireContext().filesDir, "acc.tar.gz")
                Shell.su("cp -af ${backupFile.absolutePath} ${temp.absolutePath}").exec()
                restore(temp)
            }
        )

        binding.recyclerViewBackupFiles.apply {
            adapter = backupListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.newBackup.setOnClickListener {
            if (createBackup(requireContext(), false)) {
                Toast.makeText(requireContext(), getString(R.string.backup_created), Toast.LENGTH_SHORT)
                    .show()
                backupListAdapter.setFiles(getBackupFiles())
            }
        }

        val swipeToDelete = object : SwipeToDelete(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val file = backupListAdapter.getFileAndRemoveAt(viewHolder.bindingAdapterPosition)
                val result = Shell.su(
                    "rm -f $backupFolder/$file"
                ).exec()
                if (result.isSuccess) Toast.makeText(
                    requireContext(),
                    getString(R.string.backup_deleted),
                    Toast.LENGTH_SHORT
                ).show()
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
        contents.replaceAll { s ->
            s.removePrefix("./hex").removePrefix("_")
                .removeSuffix(".apk").substringBefore('_')
        }
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

    private fun getAppList(path: String): Array<SuFile>? {
        Shell.su(
            ".$busyBox tar x -zv -f $path -C ${tempFolder.absolutePath}"
        ).exec()
        val folder = SuFile(tempFolder.absolutePath)
        return folder.listFiles { file ->
            file.length() > 0 && file.extension == "apk" && file.name.startsWith("hex")
        }
    }

    private fun restore(backupFile: File) {
        if (backupFile.extension == "gz") {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.restoring_accents),
                Toast.LENGTH_SHORT
            ).show()
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
                    requireActivity().showSnackBar(getString(R.string.accents_restored))
                }
            } else {
                Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                tempFolder = requireContext().getDir("tmp", Context.MODE_PRIVATE)

                val apps = getAppList(backupFile.absolutePath)
                val filesList = apps?.map { it.absolutePath }?.toTypedArray()
                Log.d("files-list", apps?.contentToString().toString())

                if (filesList != null && filesList.isNotEmpty()) {
                    viewModel.restore(filesList)
                    viewModel.restoreWorkerId?.let { uuid ->
                        viewModel.workManager.getWorkInfoByIdLiveData(uuid)
                            .observe(viewLifecycleOwner,
                                { workInfo ->
                                    Log.d("id", workInfo.id.toString())
                                    Log.d("state", workInfo.state.name)
                                    if (workInfo != null && workInfo.state.isFinished
                                        && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                        backupFile.delete()
                                        requireActivity().showSnackBar(
                                            getString(R.string.accents_restored)
                                        )
                                        //Shell.su("rm -rf ${tempFolder.absolutePath}").exec()
                                    }
                                    if (workInfo.state == WorkInfo.State.FAILED)
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.error_restoring),
                                            Toast.LENGTH_LONG
                                        ).show()
                                })
                    }
                } else Toast.makeText(
                    requireContext(),
                    getString(R.string.empty_backup),
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            val accentLightId = resources.getIdentifier(
                "accent_device_default_light",
                "color",
                pkgName
            )
            val accentDarkId = resources.getIdentifier(
                "accent_device_default_dark",
                "color",
                pkgName
            )
            if (accentLightId != 0 && accentDarkId != 0) {
                val colorLight = resources.getColor(accentLightId, null)
                val colorDark = resources.getColor(accentDarkId, null)
                val accent = Accent(pkgName, accentName, toHex(colorLight), toHex(colorDark))
                val accentViewModel: AccentViewModel by viewModels()
                accentViewModel.insert(accent)
            }
        }
    }
}
