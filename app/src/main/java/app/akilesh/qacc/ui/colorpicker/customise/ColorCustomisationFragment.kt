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
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorCustomisationFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.colorpicker.ColorPickerViewModel
import app.akilesh.qacc.ui.home.AccentViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar

class ColorCustomisationFragment: Fragment() {

    private lateinit var binding: ColorCustomisationFragmentBinding
    private val args: ColorCustomisationFragmentArgs by navArgs()
    private val viewModel: ColorPickerViewModel by navGraphViewModels(R.id.nav_graph)

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
        var separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        if (SDK_INT < Q) separateAccents = false

        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val systemAccent = requireContext().getColorAccent()
            setPreview(systemAccent)
        }
        viewModel.accent.observe(viewLifecycleOwner, Observer { accent ->
            accent?.let {
                setPreviewLight(Color.parseColor(it.colorLight), it.colorLight)
                if (separateAccents) setPreviewDark(Color.parseColor(it.colorDark), it.colorDark)
            }
        })

        init()
        if (args.fromHome) {
            binding.name.setText(args.accentName)
            binding.name.doAfterTextChanged {
                viewModel.accent.value!!.name = it.toString().trim()
            }
        }

        val bundle = Bundle()
        bundle.putBoolean("fromCustomise", true)
        binding.previewLight.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_edit, 0)
        binding.previewLight.root.setOnClickListener {
            bundle.putBoolean("isDark", false)
            bundle.putString("light", viewModel.accent.value!!.colorLight)
            findNavController().navigate(R.id.custom_color_picker, bundle)
        }

        if (separateAccents.not()) {
            binding.previewDark.root.visibility = View.GONE
        }
        else {
            binding.previewDark.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_edit, 0)
            binding.previewDark.root.setOnClickListener {
                bundle.putBoolean("isDark", true)
                bundle.putString("dark", viewModel.accent.value!!.colorDark)
                findNavController().navigate(R.id.custom_color_picker, bundle)
            }
        }

        binding.resetChip.setOnClickListener {
            init()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, true) {
            navigateBack()
        }
        binding.navBar.previous.setOnClickListener {
            navigateBack()
        }

        binding.navBar.next.setOnClickListener {

            if (viewModel.accent.value!!.name.isNotBlank()) {
                var suffix = "hex_" + viewModel.accent.value!!.colorLight.removePrefix("#")
                if (SDK_INT >= Q) suffix += "_" + viewModel.accent.value!!.colorDark.removePrefix("#")
                else viewModel.accent.value!!.colorDark = viewModel.accent.value!!.colorLight

                viewModel.accent.value!!.pkgName = prefix + suffix
                Log.d("accent-s", viewModel.accent.value!!.toString())
                val creatorViewModel = ViewModelProvider(this).get(ColorPickerViewModel::class.java)
                creatorViewModel.create(viewModel.accent.value!!)
                creatorViewModel.createWorkerId?.let { uuid ->
                    creatorViewModel.workManager.getWorkInfoByIdLiveData(uuid).observe(
                        viewLifecycleOwner, Observer { workInfo ->
                            Log.d("id", workInfo.id.toString())
                            Log.d("tag", workInfo.tags.toString())
                            Log.d("state", workInfo.state.name)

                            if (workInfo.state == WorkInfo.State.RUNNING && SDK_INT < P)
                                Toast.makeText(
                                    requireContext(),
                                    String.format(getString(R.string.creating, viewModel.accent.value!!.name)),
                                    Toast.LENGTH_SHORT
                                ).show()

                            if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.state.isFinished) {
                                val accentViewModel: AccentViewModel by viewModels()
                                accentViewModel.insert(viewModel.accent.value!!)
                                showSnackbar(view, String.format(getString(R.string.accent_created), viewModel.accent.value!!.name))
                                viewModel.accent.value = null
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

    private fun init() {
        viewModel.accent.value = Accent(
            String(),
            args.accentName,
            args.lightAccent,
            args.darkAccent
        )
        if (args.fromHome) binding.name.setText(args.accentName)
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
        if (SDK_INT < Q) setPreview(color)
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

    private fun navigateBack() {
        if (args.fromHome) viewModel.accent.value = null
        else init()
        findNavController().navigateUp()
    }
}
