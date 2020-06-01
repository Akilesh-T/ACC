package app.akilesh.qacc.ui.createmultiple

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.AEX
import app.akilesh.qacc.Const.Colors.brandColors
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.Const.selected
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.CreateAllFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions

private lateinit var binding: CreateAllFragmentBinding
private lateinit var viewModel: CreateMultipleViewModel

class CreateAllFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CreateAllFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val colour = if (useSystemAccent) requireContext().getColorAccent()
        else ResourcesCompat.getColor(resources, R.color.colorPrimary, requireContext().theme)
        val colorStateList = ColorStateList.valueOf(colour)
        binding.createMultipleBottomAppBar.menu.forEach {
            it.iconTintList = colorStateList
        }

        val createAllAdapter = CreateMultipleAdapter()
        createAllAdapter.colours.addAll(AEX)
        createAllAdapter.colours.addAll(brandColors)
        binding.accentListRv.apply {
            adapter = createAllAdapter
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }
        if (SDK_INT > O) {
            val rationaleHandler = createDialogRationale(R.string.app_name) {
                onPermission(
                    Permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.read_storage_permission_rationale)
                )
            }
            runWithPermissions(
                Permission.READ_EXTERNAL_STORAGE,
                rationaleHandler = rationaleHandler
            ) {
                createAllAdapter.colours.addAll(requireContext().getWallpaperColors())
                createAllAdapter.notifyDataSetChanged()
            }
        }

        val selectionTracker = SelectionTracker.Builder(
            "selection",
            binding.accentListRv,
            StableIdKeyProvider(binding.accentListRv),
            CreateMultipleItemDetailsLookup(
                binding.accentListRv
            ),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        binding.createMultipleBottomAppBar.menu.getItem(0).isVisible = selectionTracker.hasSelection()
        if (selectionTracker.hasSelection().not()) binding.createMultipleFab.hide() else binding.createMultipleFab.show()

        createAllAdapter.tracker = selectionTracker

        binding.createMultipleBottomAppBar.setOnMenuItemClickListener {
            val temp = arrayListOf<Long>()
            createAllAdapter.colours.forEachIndexed { index, _ ->
                temp.add(index.toLong())
            }
            when(it.itemId) {
                R.id.select -> {
                    selectionTracker.setItemsSelected(temp, true)
                }
                R.id.deselect -> {
                    selectionTracker.clearSelection()
                }
                R.id.invert -> {
                    temp.forEach { key ->
                        if (selectionTracker.isSelected(key)) selectionTracker.deselect(key)
                        else selectionTracker.select(key)
                    }
                }
            }
            true
        }

        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    binding.createMultipleBottomAppBar.menu.apply {
                        getItem(0).isVisible = selectionTracker.hasSelection()
                        getItem(1).isVisible = selectionTracker.hasSelection()
                    }
                    if (selectionTracker.hasSelection().not()) binding.createMultipleFab.hide() else binding.createMultipleFab.show()
                    val selection = selectionTracker.selection
                    selected = selection.mapNotNull { createAllAdapter.colours[it.toInt()] }.toSet()
                }
            }
        )

        viewModel = ViewModelProvider(this).get(CreateMultipleViewModel::class.java)
        binding.createMultipleFab.setOnClickListener {
            if (selected.isNotEmpty()) {
                Log.d("selection", selected.toString())
                viewModel.createAll()
                viewModel.createAllWorkerId?.let { uuid ->
                    viewModel.workManager.getWorkInfoByIdLiveData(uuid)
                        .observe(viewLifecycleOwner, Observer { workInfo ->
                            Log.d("id", workInfo.id.toString())
                            Log.d("state", workInfo.state.name)

                            when (workInfo.state) {
                                WorkInfo.State.RUNNING -> {
                                    binding.createMultipleFab.hide()
                                    binding.createMultipleBottomAppBar.performHide()
                                }

                                WorkInfo.State.SUCCEEDED -> {
                                    val accentViewModel =
                                        ViewModelProvider(this).get(AccentViewModel::class.java)
                                    selected.forEach { colour ->
                                        val pkgName = prefix + "hex_" + colour.hex.removePrefix("#")
                                        val accent =
                                            Accent(pkgName, colour.name, colour.hex, colour.hex)
                                        accentViewModel.insert(accent)
                                    }
                                    Toast.makeText(
                                        requireContext(),
                                        String.format(getString(R.string.accents_created)),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.action_global_home)
                                }
                                WorkInfo.State.FAILED -> {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.error),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                }
                            }
                        })
                }
            }
        }
    }
}
