package app.akilesh.qacc.ui.customisation

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
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorCustomisationFragmentBinding
import app.akilesh.qacc.databinding.CustomColorPickerBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.colorpicker.ColorPickerViewModel
import app.akilesh.qacc.ui.colorpicker.colorspace.ColorSpaceViewModel
import app.akilesh.qacc.ui.colorpicker.colorspace.CustomColorPicker
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ColorCustomisationFragment: Fragment(), CustomColorPicker {

    private lateinit var binding: ColorCustomisationFragmentBinding
    private lateinit var viewModel: CustomisationViewModel
    override lateinit var dialog: AlertDialog
    override lateinit var colorSpaceViewModel: ColorSpaceViewModel
    override lateinit var fragment: Fragment
    private val args: ColorCustomisationFragmentArgs by navArgs()
    private var separateAccents: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorCustomisationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInputLayout.visibility = if (args.fromHome) View.VISIBLE else View.GONE
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        if (SDK_INT < Q) separateAccents = false

        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val systemAccent = requireContext().getColorAccent()
            setPreview(systemAccent)
        }
        viewModel = ViewModelProvider(this).get(CustomisationViewModel::class.java)
        colorSpaceViewModel = ViewModelProvider(this).get(ColorSpaceViewModel::class.java)
        dialog = MaterialAlertDialogBuilder(requireContext()).create()
        fragment = this

        viewModel.lightAccent = args.lightAccent
        viewModel.darkAccent = args.darkAccent
        viewModel.accentName = args.accentName

        if (args.fromHome) {
            binding.name.setText(viewModel.accentName)
            binding.name.doAfterTextChanged {
                viewModel.accentName = it.toString().trim()
            }
        }

        val colorLight = Color.parseColor(viewModel.lightAccent)
        setPreviewLight(colorLight, viewModel.lightAccent)

        binding.previewLight.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_edit, 0)
        binding.previewLight.root.setOnClickListener {
            if (colorSpaceViewModel.selectedColor.value == null) colorSpaceViewModel.selectColor(colorLight)
            editAccent(false)
        }

        if (separateAccents.not() && SDK_INT < Q) {
            binding.previewDark.root.visibility = View.GONE
        }
        else {
            val colorDark = if (viewModel.darkAccent.isNotBlank()) Color.parseColor(viewModel.darkAccent) else colorLight
            setPreviewDark(colorDark, viewModel.darkAccent)
            binding.previewDark.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_edit, 0)
            binding.previewDark.root.setOnClickListener {
                if (colorSpaceViewModel.selectedColor.value == null) colorSpaceViewModel.selectColor(colorDark)
                editAccent(true)
            }
        }

        binding.resetChip.setOnClickListener {
            val action =
                ColorCustomisationFragmentDirections.reset(
                    args.lightAccent,
                    args.darkAccent,
                    args.accentName
                )
            findNavController().navigate(action)
        }

        binding.navBar.previous.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.navBar.next.setOnClickListener {

            if (viewModel.accentName.isNotBlank()) {
                var suffix = "hex_" + viewModel.lightAccent.removePrefix("#")
                if (SDK_INT >= Q) suffix += "_" + viewModel.darkAccent.removePrefix("#")
                else viewModel.darkAccent = viewModel.lightAccent

                val pkgName = prefix + suffix
                val accent = Accent(pkgName, viewModel.accentName, viewModel.lightAccent, viewModel.darkAccent)
                Log.d("accent-s", accent.toString())
                val creatorViewModel = ViewModelProvider(this).get(ColorPickerViewModel::class.java)
                creatorViewModel.create(accent)
                creatorViewModel.createWorkerId?.let { uuid ->
                    creatorViewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                        viewLifecycleOwner, Observer { workInfo ->
                            Log.d("id", workInfo.id.toString())
                            Log.d("tag", workInfo.tags.toString())
                            Log.d("state", workInfo.state.name)

                            if (workInfo.state == WorkInfo.State.RUNNING && SDK_INT < P)
                                Toast.makeText(requireContext(), String.format(getString(R.string.creating, viewModel.accentName)), Toast.LENGTH_SHORT).show()

                            if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                val accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)
                                accentViewModel.insert(accent)
                                showSnackbar(view, String.format(getString(R.string.accent_created), viewModel.accentName))
                                findNavController().navigate(R.id.action_global_home)
                            }
                            if (workInfo.state == WorkInfo.State.FAILED)
                                Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                        })
                }
            }
            else Toast.makeText(context, getString(R.string.toast_name_not_set), Toast.LENGTH_SHORT).show()
        }

    }

    private fun setPreviewLight(color: Int, hex: String) {
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
                compoundDrawableTintList = ColorStateList.valueOf(swatch.titleTextColor)
            }
            colorCard.backgroundTintList = ColorStateList.valueOf(color)
        }
        if (SDK_INT < Q || separateAccents.not()) setPreview(color)
    }

    private fun setPreviewDark(color: Int, hex: String) {
        val swatch = Palette.Swatch(color, 1)
        binding.previewDark.apply {
            colorName.apply {
                text = String.format(
                    requireContext().resources.getString(R.string.colour),
                    requireContext().resources.getString(R.string.dark),
                    hex
                )
                setTextColor(swatch.bodyTextColor)
                compoundDrawableTintList = ColorStateList.valueOf(swatch.titleTextColor)
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

    private fun editAccent(
        isDark: Boolean
    ) {
        val customColorPickerBinding = CustomColorPickerBinding.inflate(layoutInflater)
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customColorPickerBinding.root)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (colorSpaceViewModel.selectedColor.value != null) {
                    if (isDark) {
                        viewModel.darkAccent = toHex(colorSpaceViewModel.selectedColor.value!!)
                        setPreviewDark(colorSpaceViewModel.selectedColor.value!!, viewModel.darkAccent)
                    } else {
                        viewModel.lightAccent = toHex(colorSpaceViewModel.selectedColor.value!!)
                        setPreviewLight(colorSpaceViewModel.selectedColor.value!!, viewModel.lightAccent)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                colorSpaceViewModel.selectedColor.value = null
            }
            .create()

        showCustomColorPicker(
            requireContext(),
            customColorPickerBinding
        )
    }
}
