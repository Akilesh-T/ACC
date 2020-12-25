package app.akilesh.qacc.ui.colorpicker.customise

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.customHex
import app.akilesh.qacc.Const.Colors.editDark
import app.akilesh.qacc.Const.Colors.editLight
import app.akilesh.qacc.Const.Colors.editType
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.FragmentColorCustomisationBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.colorpicker.ColorPickerViewModel
import app.akilesh.qacc.ui.home.accent.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackBar

class ColorCustomisationFragment: Fragment() {

    private lateinit var binding: FragmentColorCustomisationBinding
    private val args: ColorCustomisationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentColorCustomisationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        if (SDK_INT < Q) separateAccents = false

        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val systemAccent = requireContext().getColorAccent()
            setPreview(systemAccent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navBar.root) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(systemBars() or ime()).bottom)
            insets
        }

        val navController = findNavController()
        var colorLight = args.lightAccent
        var colorDark = args.darkAccent

        setPreviewLight(colorLight)
        if (separateAccents) setPreviewDark(colorDark)

        val navBackStackEntry = navController.getBackStackEntry(R.id.customisation)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (navBackStackEntry.savedStateHandle.contains(editLight)) {
                    navBackStackEntry.savedStateHandle.getLiveData<String>(editLight)
                        .observe(viewLifecycleOwner) {
                            colorLight = it
                            setPreviewLight(it)
                    }
                }
                if (navBackStackEntry.savedStateHandle.contains(editDark)) {
                    navBackStackEntry.savedStateHandle.getLiveData<String>(editDark)
                        .observe(viewLifecycleOwner) {
                            colorDark = it
                            setPreviewDark(it)
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

        var accentName = args.accentName

        binding.name.apply {
            setText(args.accentName)
            doAfterTextChanged {
                accentName = it.toString().trim()
            }
        }


        binding.previewLight.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_outline_edit,
            0
        )
        binding.previewLight.root.setOnClickListener {
            navBackStackEntry.savedStateHandle.set<String>(editType, editLight)
            navBackStackEntry.savedStateHandle.set<String>(customHex, colorLight)
            navController.navigate(R.id.custom_color_picker)
        }

        if (separateAccents.not()) {
            binding.previewDark.root.visibility = View.GONE
        }
        else {
            binding.previewDark.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_outline_edit,
                0
            )
            binding.previewDark.root.setOnClickListener {
                navBackStackEntry.savedStateHandle.set<String>(editType, editDark)
                navBackStackEntry.savedStateHandle.set<String>(customHex, colorDark)
                navController.navigate(R.id.custom_color_picker)
            }
        }

        binding.resetChip.setOnClickListener {
            binding.name.setText(args.accentName)
            navBackStackEntry.savedStateHandle.set<String>(editLight, args.lightAccent)
            navBackStackEntry.savedStateHandle.set<String>(editDark, args.darkAccent)
        }

        binding.navBar.previous.setOnClickListener {
            navController.navigateUp()
        }

        binding.navBar.next.setOnClickListener {

            if (accentName.isNotBlank()) {
                var suffix = "hex_" + colorLight.removePrefix("#")
                if (SDK_INT >= Q) suffix += "_" + colorDark.removePrefix("#")
                else colorDark = colorLight
                val pkgName = prefix + suffix

                val accent = Accent(pkgName, accentName, colorLight, colorDark)
                Log.d("accent-s", accent.toString())
                val creatorViewModel by viewModels<ColorPickerViewModel>()
                creatorViewModel.create(accent)
                creatorViewModel.createWorkerId?.let { uuid ->
                    creatorViewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                        viewLifecycleOwner, { workInfo ->
                            Log.d("id", workInfo.id.toString())
                            Log.d("tag", workInfo.tags.toString())
                            Log.d("state", workInfo.state.name)

                            if (workInfo.state == WorkInfo.State.RUNNING && SDK_INT < P)
                                Toast.makeText(
                                    requireContext(),
                                    String.format(getString(R.string.creating, accentName)),
                                    Toast.LENGTH_SHORT
                                ).show()

                            if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                requireActivity().showSnackBar(
                                    getString(R.string.accent_created, accentName)
                                )
                                val accentViewModel: AccentViewModel by viewModels()
                                accentViewModel.insert(accent)
                                navController.navigate(R.id.action_global_home)
                            }
                            if (workInfo.state == WorkInfo.State.FAILED)
                                Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                        })
                }
            }
            else Toast.makeText(context, getString(R.string.toast_name_not_set), Toast.LENGTH_SHORT).show()
        }

    }

    private fun setPreviewLight(hex: String) {
        val color = Color.parseColor(hex)
        val accentName = if (SDK_INT < Q) args.accentName else requireContext().resources.getString(R.string.light)
        val swatch = Palette.Swatch(color, 1)

        binding.previewLight.apply {
            colorName.apply {
                text = String.format(
                    requireContext().resources.getString(R.string.colour),
                    accentName,
                    hex
                )
                setTextColor(swatch.bodyTextColor)
                TextViewCompat.setCompoundDrawableTintList(
                    this,
                    ColorStateList.valueOf(swatch.titleTextColor)
                )
            }
            colorCard.backgroundTintList = ColorStateList.valueOf(color)
        }
        if (SDK_INT < Q) setPreview(color)
    }

    private fun setPreviewDark(hex: String) {
        val color = Color.parseColor(hex)
        val swatch = Palette.Swatch(color, 1)
        binding.previewDark.apply {
            colorName.apply {
                text = String.format(
                    requireContext().resources.getString(R.string.colour),
                    requireContext().resources.getString(R.string.dark),
                    hex
                )
                setTextColor(swatch.bodyTextColor)
                TextViewCompat.setCompoundDrawableTintList(
                    this,
                    ColorStateList.valueOf(swatch.titleTextColor)
                )
            }
            colorCard.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    private fun setPreview(color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        binding.apply {
            resetChip.chipIconTint = colorStateList
            textInputLayout.apply {
                hintTextColor = colorStateList
                setBoxStrokeColorStateList(colorStateList)
            }
            if (SDK_INT >= Q) {
                name.textCursorDrawable?.setTintList(colorStateList)
                name.textSelectHandle?.setTintList(colorStateList)
                name.textSelectHandleLeft?.setTintList(colorStateList)
                name.textSelectHandleRight?.setTintList(colorStateList)
            }
            navBar.previous.setTextColor(color)
            navBar.previous.rippleColor = colorStateList
            navBar.next.backgroundTintList = colorStateList
        }
    }
}
