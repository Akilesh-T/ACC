package app.akilesh.qacc.ui.colorpicker

import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.AEX
import app.akilesh.qacc.Const.Colors.brandColors
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.ui.home.AccentViewModel
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions

class ColorPickerFragment: Fragment(),
    ColorPicker {

    override lateinit var binding: ColorPickerFragmentBinding
    override lateinit var viewModel: ColorPickerViewModel

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

        val previewColor = if (viewModel.colour.hex.isBlank()) requireContext().getColorAccent()
        else Color.parseColor(viewModel.colour.hex)
        setPreview(binding, previewColor)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        val customise = sharedPreferences.getBoolean("customise", false)
        if (SDK_INT < Q) separateAccents = false

        if (separateAccents) {
            binding.title.text = String.format(getString(R.string.picker_title_text_light))
            binding.navBar.next.text = getString(R.string.next)
            binding.nameTitle.visibility = View.GONE
            binding.textInputLayout.visibility = View.GONE
        }
        else
            binding.title.text = String.format(getString(R.string.picker_title_text))

        if (customise)
            binding.navBar.next.text = getString(R.string.next)

        binding.navBar.next.setOnClickListener {

            if (separateAccents) {
                if (viewModel.colour.hex.isNotBlank()) {
                    val action =
                        ColorPickerFragmentDirections.toDark(
                            viewModel.colour.hex
                        )
                    findNavController().navigate(action)
                }
                else
                    Toast.makeText(context, getString(R.string.toast_light_theme_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
            }
            else if (customise) {
                if (viewModel.colour.hex.isNotBlank() && viewModel.colour.name.isNotBlank()) {
                    val action =
                        ColorPickerFragmentDirections.toCustomise(
                            viewModel.colour.hex,
                            viewModel.colour.name,
                            viewModel.colour.hex
                        )
                    findNavController().navigate(action)
                }
                else {
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
            else {
                if (viewModel.colour.hex.isNotBlank() && viewModel.colour.name.isNotBlank()) {
                    val suffix = "hex_" + viewModel.colour.hex.removePrefix("#")
                    val pkgName = prefix + suffix
                    val accent = Accent(pkgName, viewModel.colour.name, viewModel.colour.hex, viewModel.colour.hex)
                    Log.d("accent-s", accent.toString())
                    viewModel.create(accent)
                    viewModel.createWorkerId?.let { uuid ->
                        viewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                            viewLifecycleOwner, Observer { workInfo ->
                                Log.d("id", workInfo.id.toString())
                                Log.d("tag", workInfo.tags.toString())
                                Log.d("state", workInfo.state.name)

                                if (workInfo.state == WorkInfo.State.RUNNING && SDK_INT < P)
                                    Toast.makeText(requireContext(), String.format(getString(R.string.creating, viewModel.colour.name)), Toast.LENGTH_SHORT).show()

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

        binding.brandColors.setOnClickListener { showColorPickerDialog(requireContext(), layoutInflater, R.string.brand_colors, R.drawable.ic_ferrari, brandColors) }
        binding.custom.setOnClickListener {
            customColorPicker(previewColor, parentFragmentManager)
        }
        binding.mdcColors.setOnClickListener {
            showTabDialog(requireContext(), layoutInflater, binding)
        }
        binding.preset.setOnClickListener { showColorPickerDialog(requireContext(), layoutInflater, R.string.presets, R.drawable.ic_preset, AEX) }
        if (SDK_INT > O)
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
                        showColorPickerDialog(requireContext(), layoutInflater, R.string.color_wallpaper, R.drawable.ic_wallpaper, requireContext().getWallpaperColors())
                    }
                }
            }
        else
            binding.wallColors.visibility = View.GONE

        binding.name.doAfterTextChanged {
            viewModel.colour.name = it.toString().trim()
        }
    }
}
