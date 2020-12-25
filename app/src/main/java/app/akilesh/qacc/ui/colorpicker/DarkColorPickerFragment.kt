package app.akilesh.qacc.ui.colorpicker

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.Colors.AEX
import app.akilesh.qacc.Const.Colors.brandColors
import app.akilesh.qacc.Const.Colors.colorList
import app.akilesh.qacc.Const.Colors.customHex
import app.akilesh.qacc.Const.Colors.selectedColor
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.FragmentColorPickerBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.home.accent.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.showSnackBar
import app.akilesh.qacc.utils.AppUtils.toHex
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions

class DarkColorPickerFragment: Fragment() {

    private lateinit var binding: FragmentColorPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPreview(binding, Colour(toHex(requireContext().getColorAccent()), ""), true)

        val args: DarkColorPickerFragmentArgs by navArgs()
        val colorLight = args.colorLight
        if (colorLight.name.isNotBlank()) binding.name.setText(colorLight.name)

        var colorDark = Colour("", "")
        var colorName = ""
        val navController = findNavController()

        val navBackStackEntry = navController.getBackStackEntry(R.id.dark_accent)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains(selectedColor)) {
                navBackStackEntry.savedStateHandle.get<Colour>(selectedColor)?.let {
                    Log.d(selectedColor, it.toString())
                    setPreview(binding, it)
                    colorDark = it
                    colorName = it.name
                }
            }
        }

        navBackStackEntry.lifecycle.addObserver(observer)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })


        binding.theme.setImageDrawable(ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.ic_outline_nights_stay,
            requireContext().theme
        ))


        ViewCompat.setOnApplyWindowInsetsListener(binding.navBar.root) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(systemBars() or ime()).bottom)
            insets
        }

        binding.navBar.next.setOnClickListener {

                if (colorName.isNotBlank()
                    && colorDark.hex.isNotBlank()) {

                    var suffix = "hex_" + colorLight.hex.removePrefix("#")
                    if (colorLight.hex != colorDark.hex) {
                        suffix += "_" + colorDark.hex.removePrefix("#")
                    }
                    val pkgName = prefix + suffix
                    val accent = Accent(pkgName, colorName, colorLight.hex, colorDark.hex)
                    Log.d("accent-s", accent.toString())

                    val viewModel by viewModels<ColorPickerViewModel>()

                    viewModel.create(accent)
                    viewModel.createWorkerId?.let { uuid ->
                        viewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                            viewLifecycleOwner, { workInfo ->
                                Log.d("id", workInfo.id.toString())
                                Log.d("tag", workInfo.tags.toString())
                                Log.d("state", workInfo.state.name)

                                if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                    requireActivity().showSnackBar(
                                        getString(R.string.accent_created, accent.name)
                                    )
                                    val accentViewModel: AccentViewModel by viewModels()
                                    accentViewModel.insert(accent)
                                    navController.navigate(R.id.action_global_home)
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
                    if (colorDark.hex.isBlank()) Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_color_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (colorName.isBlank()) Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_name_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }

        binding.navBar.previous.setOnClickListener {
            navController.navigateUp()
        }


        binding.brandColors.setOnClickListener {
            navBackStackEntry.savedStateHandle.set(colorList, brandColors)
            navController.navigate(R.id.color_picker_dialog)
        }

        binding.custom.setOnClickListener {
            navBackStackEntry.savedStateHandle.set<String>(customHex, colorDark.hex)
            navController.navigate(R.id.custom_color_picker)
        }

        binding.mdcColors.setOnClickListener {
            navController.navigate(R.id.color_palette_dialog)
        }
        binding.preset.setOnClickListener {
            navBackStackEntry.savedStateHandle.set(colorList, AEX)
            navController.navigate(R.id.color_picker_dialog)
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
                        navBackStackEntry.savedStateHandle.set(
                            colorList, requireContext().getWallpaperColors()
                        )
                        navController.navigate(R.id.color_picker_dialog)
                    }
                }
            }
        else
            binding.wallColors.visibility = View.GONE

        binding.name.doAfterTextChanged {
            colorName = it.toString().trim()
        }
    }
}
