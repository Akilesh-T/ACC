package app.akilesh.qacc.ui.home.accent.info

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.SheetAccentInfoBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.AccentInfo
import app.akilesh.qacc.ui.home.accent.AccentViewModel
import app.akilesh.qacc.utils.OverlayUtils.isOverlayInstalled
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

class AccentInfoSheet: BottomSheetDialogFragment() {

    private lateinit var binding: SheetAccentInfoBinding
    private val navArgs: AccentInfoSheetArgs by navArgs()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetAccentInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accent = Accent(
            pkgName = navArgs.pkgName,
            name = navArgs.accentName,
            colorLight = navArgs.lightAccent,
            colorDark = navArgs.darkAccent
        )
        val color = if (SDK_INT >= Q && accent.colorLight != accent.colorDark) {
            when (requireContext().resources?.configuration?.uiMode?.
            and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> accent.colorDark
                Configuration.UI_MODE_NIGHT_NO -> accent.colorLight
                else -> accent.colorLight
            }
        } else accent.colorLight
        val tintColor = ColorStateList.valueOf(Color.parseColor(color))

        binding.apply {
            edit.setTextColor(tintColor)
            edit.rippleColor = tintColor.withAlpha(30)

            delete.backgroundTintList = tintColor
        }

        val accentInfoAdapter = AccentInfoAdapter()
        navController = findNavController()

        binding.apply {
            edit.setOnClickListener {
                val editNavDirections =
                    AccentInfoSheetDirections.edit(
                        accent.colorLight,
                        accent.colorDark,
                        accent.name
                    )
                navController.navigate(editNavDirections)
            }

            delete.setOnClickListener {
                removeAccent(accent)
                dismiss()
            }

            infoRecyclerView.adapter = accentInfoAdapter
        }
        accentInfoAdapter.submitList(accent.getInfo())

    }

    private fun Accent.getInfo(): List<AccentInfo> {
        val packageManager = requireContext().packageManager
        val isInstalled = packageManager.isOverlayInstalled(pkgName)

        val infoList = mutableListOf(
            AccentInfo("Name", name),
            AccentInfo("Package name", pkgName),
        )
        if (isInstalled) {
            val packageInfo = packageManager.getPackageInfo(pkgName, 0)
            infoList.addAll(
                listOf(
                    AccentInfo("Version", packageInfo.versionName),
                    AccentInfo("Path to APK", packageInfo.applicationInfo.sourceDir),
                    AccentInfo("First installed", DateUtils.getRelativeTimeSpanString(
                        packageInfo.firstInstallTime,
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS
                    ).toString())
                )
            )
        }

        return infoList
    }

    private fun removeAccent(accent: Accent) {
        val accentViewModel by viewModels<AccentViewModel>()
        val previousSavedStateHandle = navController.previousBackStackEntry?.savedStateHandle
        val appName = accent.pkgName.substringAfter(prefix)

        if (SDK_INT >= P) {
            Shell.su(
                "rm -f $overlayPath/$appName.apk"
            ).exec().apply {
                if(isSuccess) {
                    previousSavedStateHandle?.set("uninstall", Pair(accent.name, isSuccess))
                    accentViewModel.delete(accent)
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.uninstalling, accent.name),
                Toast.LENGTH_SHORT
            ).show()
            viewLifecycleOwner.lifecycleScope.launch {
                Shell.su("pm uninstall ${accent.pkgName}").exec().apply {
                    Log.d("pm-uninstall", "${accent.name} - $out")
                    if(isSuccess) {
                        previousSavedStateHandle?.set("uninstall", Pair(accent.name, isSuccess))
                        accentViewModel.delete(accent)
                    }
                }
            }
        }
    }
}
