package app.akilesh.qacc.ui.colorpicker

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.brandColorsArg
import app.akilesh.qacc.Const.Colors.listArg
import app.akilesh.qacc.Const.Colors.presetsArg
import app.akilesh.qacc.Const.Colors.wallpaperColorsArg
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions

class DarkColorPickerFragment: Fragment() {

    private lateinit var binding: ColorPickerFragmentBinding
    private val viewModel: ColorPickerViewModel by navGraphViewModels(R.id.nav_graph)

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

        setPreview(binding, Colour(toHex(requireContext().getColorAccent()), ""), true)
        viewModel.accent.observe(viewLifecycleOwner, Observer { accent ->
            accent?.let {
                if (it.colorDark.isNotBlank()) setPreview(binding, Colour(it.colorDark, it.name))
            }
        })

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val customise = sharedPreferences.getBoolean("customise", false)
        binding.theme.setImageDrawable(ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_outline_nights_stay, requireContext().theme))

        if (customise) binding.navBar.next.text = requireContext().resources.getString(R.string.next)

        binding.navBar.next.setOnClickListener {

            if (viewModel.accent.value != null) {
                if (customise) {
                    if (viewModel.accent.value!!.name.isNotBlank()
                        && viewModel.accent.value!!.colorDark.isNotBlank()) {
                        val action =
                            DarkColorPickerFragmentDirections.toCustomise(
                                viewModel.accent.value!!.colorLight,
                                viewModel.accent.value!!.colorDark,
                                viewModel.accent.value!!.name
                            )
                        findNavController().navigate(action)
                    } else {
                        if (viewModel.accent.value!!.colorDark.isBlank()) Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_color_not_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (viewModel.accent.value!!.name.isBlank()) Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_name_not_set),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    if (viewModel.accent.value!!.colorDark.isNotBlank() && viewModel.accent.value!!.name.isNotBlank()) {
                        var suffix = "hex_" + viewModel.accent.value!!.colorLight.removePrefix("#")
                        if (viewModel.accent.value!!.colorLight != viewModel.accent.value!!.colorDark) {
                            suffix += "_" + viewModel.accent.value!!.colorDark.removePrefix("#")
                        }
                        viewModel.accent.value!!.pkgName = prefix + suffix
                        Log.d("accent-s", viewModel.accent.value.toString())
                        viewModel.create(viewModel.accent.value!!)
                        viewModel.createWorkerId?.let { uuid ->
                            viewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                                viewLifecycleOwner, Observer { workInfo ->
                                    Log.d("id", workInfo.id.toString())
                                    Log.d("tag", workInfo.tags.toString())
                                    Log.d("state", workInfo.state.name)

                                    if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                        val accentViewModel: AccentViewModel by viewModels()
                                        accentViewModel.insert(viewModel.accent.value!!)
                                        showSnackbar(
                                            view,
                                            String.format(
                                                getString(R.string.accent_created),
                                                viewModel.accent.value!!.name
                                            )
                                        )
                                        viewModel.accent.value = null
                                        findNavController().navigate(R.id.action_global_home)
                                    }
                                    if (workInfo.state == WorkInfo.State.FAILED)
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.error),
                                            Toast.LENGTH_LONG
                                        ).show()
                                })
                        }
                    } else {
                        if (viewModel.accent.value!!.colorDark.isBlank()) Toast.makeText(
                            context,
                            getString(R.string.toast_color_not_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (viewModel.accent.value!!.name.isBlank()) Toast.makeText(
                            context,
                            getString(R.string.toast_name_not_set),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, true) {
            navigateBack()
        }
        binding.navBar.previous.setOnClickListener {
            navigateBack()
        }

        val bundle = Bundle()
        bundle.putBoolean("isDark", true)

        binding.brandColors.setOnClickListener {
            bundle.putString(listArg, brandColorsArg)
            findNavController().navigate(R.id.color_picker_dialog, bundle)
        }

        binding.custom.setOnClickListener {
            findNavController().navigate(R.id.custom_color_picker, bundle)
        }

        binding.mdcColors.setOnClickListener {
            findNavController().navigate(R.id.color_palette_dialog, bundle)
        }
        binding.preset.setOnClickListener {
            bundle.putString(listArg, presetsArg)
            findNavController().navigate(R.id.color_picker_dialog, bundle)
        }
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
                        bundle.putString(listArg, wallpaperColorsArg)
                        findNavController().navigate(R.id.color_picker_dialog, bundle)
                    }
                }
            }
        else
            binding.wallColors.visibility = View.GONE

        binding.name.doAfterTextChanged {
            viewModel.accent.value?.let { color ->
                color.name = it.toString().trim()
            }
        }
    }

    private fun navigateBack() {
        viewModel.accent.value!!.apply {
            colorDark = String()
            name = String()
        }
        findNavController().navigateUp()
    }
}
