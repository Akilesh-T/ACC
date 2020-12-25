package app.akilesh.qacc.ui.home

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.FragmentHomeBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.home.accent.AccentListAdapter
import app.akilesh.qacc.ui.home.accent.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackBar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.OverlayUtils.disableAccent
import app.akilesh.qacc.utils.OverlayUtils.enableAccent
import app.akilesh.qacc.utils.OverlayUtils.getInstalledOverlays
import app.akilesh.qacc.utils.OverlayUtils.isOverlayEnabled
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment: Fragment() {

    private val accentViewModel: AccentViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.updatePadding(top = insets.getInsets(systemBars()).top)
            insets
        }

        val navController = findNavController()
        val clickListeners = AccentListAdapter.ClickListeners(
            { pkgName ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isOverlayEnabled(pkgName)) disableAccent(pkgName)
                    else enableAccent(pkgName)
                }
            },
            { accent ->
                val infoNavDirections = HomeFragmentDirections.actionHomeToAccentInfo(
                   accent.colorLight, accent.colorDark, accent.name, accent.pkgName
               )
                navController.navigate(infoNavDirections)
            }
        )

        val accentListAdapter = AccentListAdapter(clickListeners)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)

        binding.recyclerView.apply {
            adapter = accentListAdapter
            if (useSystemAccent) {
                edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                        return EdgeEffect(view.context).apply {
                            color = requireContext().getColorAccent()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            accentViewModel.allAccents.collectLatest { pagingData ->
                withContext(Dispatchers.Main) {
                    accentListAdapter.submitData(pagingData)
                }
            }
        }

        checkForMissingAccents()

        val navBackStackEntry = navController.getBackStackEntry(R.id.home)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains("uninstall")
            ) {
                val savedStateHandle = navBackStackEntry.savedStateHandle
                val uninstallListener = savedStateHandle.get<Pair<String, Boolean>>("uninstall")
                uninstallListener?.let {
                    if (it.second) {
                        requireActivity().showSnackBar(getString(R.string.accent_removed, it.first))
                        savedStateHandle.set("uninstall", Pair("", false))
                    }
                }

            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun checkForMissingAccents() {
        val installed = getInstalledOverlays()

        if (SDK_INT >= P) {
            val list = Shell.su("ls -1 $overlayPath").exec().out
            if (list.isNotEmpty()) {
                val inModule = list.map {
                    prefix + it.removeSuffix(".apk")
                }
                val deleted = installed.subtract(inModule)
                if (deleted.isNotEmpty()) {
                    installed.removeAll(deleted)
                }
            }
        }

        installed.forEach {
            viewLifecycleOwner.lifecycleScope.launch {
                if (accentViewModel.accentExists(it).not()) {
                    insertMissing(it)
                }
            }
        }
    }

    private fun insertMissing(pkgName: String) {
        Log.w("Missing in db", pkgName)
        val packageInfo = requireContext().packageManager.getPackageInfo(pkgName, 0)
        val applicationInfo = packageInfo.applicationInfo
        val accentName =
            requireContext().packageManager.getApplicationLabel(applicationInfo).toString()
        val resources = requireContext().packageManager.getResourcesForApplication(applicationInfo)
        val accentLightId =
            resources.getIdentifier("accent_device_default_light", "color", pkgName)
        val accentDarkId =
            resources.getIdentifier("accent_device_default_dark", "color", pkgName)
        if (accentLightId != 0 && accentDarkId != 0) {
            val colorLight = resources.getColor(accentLightId, null)
            val colorDark = resources.getColor(accentDarkId, null)
            val accent = Accent(
                pkgName,
                accentName,
                toHex(colorLight),
                toHex(colorDark)
            )
            Log.d("Inserting to db", accent.toString())
            accentViewModel.insert(accent)
        }
    }
}
