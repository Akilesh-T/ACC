package app.akilesh.qacc.ui.colorpicker

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.AEX
import app.akilesh.qacc.Const.Colors.brandColors
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.CustomColorPickerBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.colorpicker.colorspace.ColorSpaceViewModel
import app.akilesh.qacc.ui.colorpicker.colorspace.CustomColorPicker
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DarkColorPickerFragment: Fragment(),
    ColorPicker, CustomColorPicker {

    override lateinit var binding: ColorPickerFragmentBinding
    override lateinit var viewModel: ColorPickerViewModel
    private val args: DarkColorPickerFragmentArgs by navArgs()
    override lateinit var dialog: AlertDialog
    override lateinit var colorSpaceViewModel: ColorSpaceViewModel
    override lateinit var fragment: Fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorPickerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ColorPickerViewModel::class.java)
        dialog = MaterialAlertDialogBuilder(requireContext()).create()
        colorSpaceViewModel = ViewModelProvider(this).get(ColorSpaceViewModel::class.java)
        fragment = this

        val previewColor = if (viewModel.colour.hex.isBlank()) requireContext().getColorAccent()
        else Color.parseColor(viewModel.colour.hex)
        setPreview(binding, previewColor)

        viewModel.accentLight = args.lightAccent

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val customise = sharedPreferences.getBoolean("customise", false)
        binding.theme.setImageDrawable(ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_nights_stay, requireContext().theme))

        if (customise) binding.navBar.next.text = requireContext().resources.getString(R.string.next)


        binding.navBar.next.setOnClickListener {

            if (customise) {
                if (viewModel.colour.name.isNotBlank() && viewModel.colour.hex.isNotBlank()) {
                    val action =
                        DarkColorPickerFragmentDirections.toCustomise(
                            viewModel.accentLight,
                            viewModel.colour.hex,
                            viewModel.colour.name
                        )
                    findNavController().navigate(action)
                } else {
                    if (viewModel.colour.hex.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_color_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (viewModel.colour.name.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_name_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

                if (viewModel.colour.hex.isNotBlank() && viewModel.colour.name.isNotBlank()) {
                    var suffix = "hex_" + viewModel.accentLight.removePrefix("#")
                    if (viewModel.accentLight != viewModel.colour.hex) {
                        suffix += "_" + viewModel.colour.hex.removePrefix("#")
                    }
                    val pkgName = prefix + suffix
                    val accent = Accent(pkgName, viewModel.colour.name, viewModel.accentLight, viewModel.colour.hex)
                    Log.d("accent-s", accent.toString())
                    viewModel.create(accent)
                    viewModel.createWorkerId?.let { uuid ->
                        viewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                            viewLifecycleOwner, Observer { workInfo ->
                                Log.d("id", workInfo.id.toString())
                                Log.d("tag", workInfo.tags.toString())
                                Log.d("state", workInfo.state.name)

                                if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                    val accentViewModel = ViewModelProvider(this).get(
                                        AccentViewModel::class.java)
                                    accentViewModel.insert(accent)
                                    showSnackbar(view, String.format(getString(R.string.accent_created), viewModel.colour.name))
                                    findNavController().navigate(R.id.action_global_home)
                                }
                                if (workInfo.state == WorkInfo.State.FAILED)
                                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                            })
                    }
                } else {
                    if (viewModel.colour.hex.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_color_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (viewModel.colour.name.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_name_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.navBar.previous.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.brandColors.setOnClickListener {
            showColorPickerDialog(
                requireContext(),
                layoutInflater,
                binding.brandColorsText.text.toString(),
                R.drawable.ic_palette_24dp,
                brandColors
            )
        }

        binding.custom.setOnClickListener {
            val customColorPickerBinding = CustomColorPickerBinding.inflate(layoutInflater)
            dialog = createDialog(customColorPickerBinding, colorSpaceViewModel)
            showCustomColorPicker(
                requireContext(),
                customColorPickerBinding
            )
        }

        binding.mdcColors.setOnClickListener {
            showTabDialog(requireContext(), layoutInflater, binding)
        }
        binding.preset.setOnClickListener {
            showColorPickerDialog(
                requireContext(),
                layoutInflater,
                binding.presetText.text.toString(),
                R.drawable.ic_preset,
                AEX
            )
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            binding.wallColors.setOnClickListener {
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
                    if (it.isAllGranted()) {
                        showColorPickerDialog(
                            requireContext(),
                            layoutInflater,
                            binding.wallColorsText.text.toString(),
                            R.drawable.ic_wallpaper,
                            requireContext().getWallpaperColors())
                    }
                }
            }
        else
            binding.wallColors.visibility = View.GONE

        binding.name.doAfterTextChanged {
            viewModel.colour.name = it.toString().trim()
        }
    }

    private fun createDialog(
        customColorPickerBinding: CustomColorPickerBinding,
        colorSpaceViewModel: ColorSpaceViewModel
    ): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(customColorPickerBinding.root)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (colorSpaceViewModel.selectedColor.value != null) {
                    setPreview(
                        binding,
                        colorSpaceViewModel.selectedColor.value!!
                    )
                    viewModel.colour.hex = toHex(colorSpaceViewModel.selectedColor.value!!)
                    Log.d("custom-hex", viewModel.colour.hex)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                colorSpaceViewModel.selectedColor.value = null
            }
            .create()
    }
}
