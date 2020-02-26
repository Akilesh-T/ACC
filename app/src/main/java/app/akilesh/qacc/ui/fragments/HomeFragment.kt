package app.akilesh.qacc.ui.fragments

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Paths.overlayPath
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.HomeFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.adapter.AccentListAdapter
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.utils.SwipeToDelete
import app.akilesh.qacc.viewmodel.AccentViewModel
import com.topjohnwu.superuser.Shell


class HomeFragment: Fragment() {

    private lateinit var accentViewModel: AccentViewModel
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AccentListAdapter(requireContext()) {
            val navDirections = HomeFragmentDirections.edit(it.colorLight, it.colorDark, it.name, true)
            findNavController().navigate(navDirections)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)
        accentViewModel.allAccents.observe(viewLifecycleOwner, Observer { accents ->
            accents?.let { adapter.setAccents(it) }
            insertMissing(accents)
        })

        val swipeHandler = object : SwipeToDelete(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               val accent = adapter.getAccentAndRemoveAt(viewHolder.adapterPosition)
                accentViewModel.delete(accent)
                val appName = accent.pkgName.substringAfter(prefix)
                val result = if (SDK_INT >= P)
                    Shell.su("rm -f $overlayPath/$appName.apk").exec()
                else
                    Shell.su(
                        "cmd overlay disable ${accent.pkgName}",
                        "pm uninstall ${accent.pkgName}"
                    ).exec()
                if (result.isSuccess)
                    showSnackbar(view, String.format(getString(R.string.accent_removed), accent.name))
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun insertMissing(accents: MutableList<Accent>) {
        val inDB = mutableSetOf<String>()
        val installed = mutableSetOf<String>()

        if (accents.isNotEmpty())
           inDB.addAll(accents.map { it.pkgName })

        val installedAccents: MutableList<String> =  Shell.su(
            "pm list packages -f $prefix | sed s/package://"
        ).exec().out

        if (installedAccents.isNotEmpty())
            installed.addAll(
                    installedAccents.map { it.substringAfterLast('=') }
            )
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

        val missingAccents = installed.subtract(inDB)
        if (missingAccents.isNotEmpty()) {
            Log.w("Missing in db", missingAccents.toString())
            missingAccents.forEach { pkgName ->
                val packageInfo = requireContext().packageManager.getPackageInfo(pkgName, 0)
                val applicationInfo = packageInfo.applicationInfo
                val accentName =
                    requireContext().packageManager.getApplicationLabel(applicationInfo).toString()
                val resources = requireContext().packageManager.getResourcesForApplication(applicationInfo)
                val accentLightId =
                    resources.getIdentifier("accent_device_default_light", "color", pkgName)
                val colorLight = resources.getColor(accentLightId, null)
                val accentDarkId =
                    resources.getIdentifier("accent_device_default_dark", "color", pkgName)
                val colorDark = resources.getColor(accentDarkId, null)
                val accent = Accent(
                    pkgName,
                    accentName,
                    toHex(colorLight),
                    toHex(colorDark)
                )
                Log.d("inserting accent...", accent.toString())
                accentViewModel.insert(accent)
            }
        }
    }
}