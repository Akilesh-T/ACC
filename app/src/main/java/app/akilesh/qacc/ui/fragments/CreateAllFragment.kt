package app.akilesh.qacc.ui.fragments

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
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
import app.akilesh.qacc.Const.selected
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.CreateAllFragmentBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.adapter.CreateAllAdapter
import app.akilesh.qacc.ui.adapter.ItemDetailsLookupUtil
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import app.akilesh.qacc.viewmodel.CreatorViewModel
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions

private lateinit var binding: CreateAllFragmentBinding

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
        binding.sheetContent.progress.indeterminateTintList = colorStateList
        binding.sheetContent.apply {
            selection.apply {
                setTextColor(colour)
                rippleColor = colorStateList
            }
            create.apply {
                setTextColor(colour)
                rippleColor = colorStateList
            }
        }

        val allColours = mutableListOf<Colour>()
        allColours.addAll(AEX)
        allColours.addAll(brandColors)

        val createAllAdapter = CreateAllAdapter(requireContext())
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
                allColours.addAll(requireContext().getWallpaperColors())
                createAllAdapter.colours = allColours
                createAllAdapter.notifyDataSetChanged()
            }
        }

        val selectionTracker = SelectionTracker.Builder(
            "selection",
            binding.accentListRv,
            StableIdKeyProvider(binding.accentListRv),
            ItemDetailsLookupUtil(binding.accentListRv),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        createAllAdapter.apply {
            colours = allColours
            tracker = selectionTracker
        }

        val temp = arrayListOf<Long>()
        allColours.forEachIndexed { index, _ ->
            temp.add(index.toLong())
        }
        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val atLeastOne = selectionTracker.selection.size() > 0
                    val selection = selectionTracker.selection
                    selected = selection.mapNotNull { createAllAdapter.colours[it.toInt()] }.toSet()

                    binding.sheetContent.selection.apply {
                        text =
                            if (atLeastOne)
                                requireContext().getString(R.string.deselect_all)
                            else requireContext().getString(R.string.select_all)

                        setOnClickListener {
                            selectionTracker.setItemsSelected(temp,  atLeastOne.not())
                        }
                    }
                    binding.sheetContent.create.visibility =
                        if (atLeastOne) View.VISIBLE
                        else View.GONE
                }
            }
        )

        binding.fab.setOnClickListener {
            binding.fab.isExpanded = true
        }
        binding.sheetContent.close.setOnClickListener {
            binding.fab.isExpanded = false
        }

        binding.sheetContent.create.setOnClickListener {
            Log.d("selection", selected.toString())
            val creatorViewModel = ViewModelProvider(this).get(CreatorViewModel::class.java)
            binding.sheetContent.apply {
                close.visibility = View.GONE
                selection.visibility = View.GONE
                create.visibility = View.GONE
                progress.visibility = View.VISIBLE
            }
            creatorViewModel.createAll()
            creatorViewModel.outputWorkInfo.observe(viewLifecycleOwner, Observer { listOfWorkInfo ->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                val workInfo = listOfWorkInfo[0]
                if (workInfo.state.isFinished && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    /*
                    ~ Accents will be inserted to db automatically after installation.

                    val accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)
                    selected.forEach { colour ->
                        val pkgName = prefix + "hex_" + colour.hex.removePrefix("#")
                        val accent = Accent(pkgName, colour.name, colour.hex, colour.hex)
                        accentViewModel.insert(accent)
                    }
                    */
                    Handler().postDelayed({
                        binding.sheetContent.apply {
                            close.visibility = View.VISIBLE
                            selection.visibility = View.VISIBLE
                            create.visibility = View.VISIBLE
                            progress.visibility = View.GONE
                            selection.performClick()
                            close.performClick()
                        }
                        Toast.makeText(requireContext(),
                            String.format(getString(R.string.accents_created)),
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigate(R.id.action_global_home)
                    }, 1000)
                }
            })
        }
    }
}
