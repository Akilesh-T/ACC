package app.akilesh.qacc.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorCustomisationFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.viewmodel.AccentViewModel
import app.akilesh.qacc.viewmodel.CustomisationViewModel
import kotlin.properties.Delegates

class ColorCustomisationFragment: Fragment() {

    private lateinit var binding: ColorCustomisationFragmentBinding
    private lateinit var model: CustomisationViewModel
    private lateinit var accentViewModel: AccentViewModel
    private val args: ColorCustomisationFragmentArgs by navArgs()
    private var colorLight by Delegates.notNull<Int>()
    private var colorDark by Delegates.notNull<Int>()
    private var separateAccents by Delegates.notNull<Boolean>()

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        if (SDK_INT < Q) separateAccents = false

        var accentLight = args.lightAccent
        var accentDark = args.darkAccent
        var accentName = args.accentName

        if (args.fromHome) {
            binding.name.setText(accentName)
            binding.name.doAfterTextChanged {
                accentName = it.toString().trim()
            }
        }

        colorLight = Color.parseColor(accentLight)
        setPreviewLight(colorLight, accentLight)
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(colorLight, hsl)
        binding.lightSliders.hue.value = hsl[0]
        binding.lightSliders.saturation.value = hsl[1]
        binding.lightSliders.lightness.value = hsl[2]

        model = ViewModelProvider(this).get(CustomisationViewModel::class.java)
        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)

        val lightAccentObserver = Observer<String> {
            accentLight = it
            colorLight = Color.parseColor(accentLight)
            setPreviewLight(colorLight, accentLight)
        }
        model.lightAccent.observe(viewLifecycleOwner, lightAccentObserver)
        editAccentLight()

        if (!separateAccents && SDK_INT < Q) {
            binding.toggleButton.visibility = View.GONE
            binding.previewDark.root.visibility = View.GONE
        }
        else {
            colorDark = if (accentDark.isNotBlank()) Color.parseColor(accentDark) else colorLight
            setPreviewDark(colorDark, accentDark)
            ColorUtils.colorToHSL(colorDark, hsl)
            binding.darkSliders.hue.value = hsl[0]
            binding.darkSliders.saturation.value = hsl[1]
            binding.darkSliders.lightness.value = hsl[2]

            val darkAccentObserver = Observer<String> {
                accentDark = it
                colorDark = Color.parseColor(accentDark)
                setPreviewDark(colorDark, accentDark)
            }
            model.darkAccent.observe(viewLifecycleOwner, darkAccentObserver)
        }

        binding.toggleButton.addOnButtonCheckedListener { group, _, _ ->
            when(group.checkedButtonId) {
                binding.lightToggle.id -> {
                    binding.lightSliders.root.visibility = View.VISIBLE
                    binding.darkSliders.root.visibility = View.GONE
                    editAccentLight()
                }
                binding.darkToggle.id -> {
                    binding.lightSliders.root.visibility = View.GONE
                    binding.darkSliders.root.visibility = View.VISIBLE
                    editAccentDark()
                }
            }
        }

        binding.resetChip.setOnClickListener {
            val action = ColorCustomisationFragmentDirections.reset(args.lightAccent, args.darkAccent, args.accentName)
            findNavController().navigate(action)
        }

        binding.buttonPrevious.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonNext.setOnClickListener {

            if (accentName.isNotBlank()) {
                var suffix = "hex_" + accentLight.removePrefix("#")
                val dark: String
                if (SDK_INT < Q)
                    dark = accentLight
                else {
                    suffix += "_" + accentDark.removePrefix("#")
                    dark = accentDark
                }
                val pkgName = prefix + suffix
                val accent = Accent(pkgName, accentName, accentLight, dark)
                Log.d("accent", accent.toString())
                if (createAccent(requireContext(), accentViewModel, accent)) {
                    showSnackbar(
                        view,
                        String.format(getString(R.string.accent_created), args.accentName)
                    )
                    findNavController().navigate(R.id.to_home)
                }
            }
            else Toast.makeText(context, getString(R.string.toast_name_not_set), Toast.LENGTH_SHORT).show()
        }

    }

    private fun setPreviewLight(color: Int, hex: String) {
        val colorName = if (SDK_INT < Q) args.accentName else requireContext().resources.getString(R.string.light)
        binding.previewLight.colorName.text = String.format(requireContext().resources.getString(R.string.colour), colorName, hex)
        val textColorLight = Palette.Swatch(color, 1).bodyTextColor
        binding.previewLight.colorName.setTextColor(textColorLight)
        binding.previewLight.colorCard.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun setPreviewDark(color: Int, hex: String) {
        binding.previewDark.colorName.text = String.format(requireContext().resources.getString(R.string.colour), requireContext().resources.getString(R.string.dark), hex)
        val textColorDark = Palette.Swatch(color, 1).bodyTextColor
        binding.previewDark.colorName.setTextColor(textColorDark)
        binding.previewDark.colorCard.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun editAccentLight() {

        binding.lightSliders.root.visibility = View.VISIBLE
        binding.darkSliders.root.visibility = View.GONE

        var newColor: Int
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(colorLight, hsl)

        binding.lightSliders.hue.addOnChangeListener { _, value, _ ->
            hsl[0] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.lightAccent.value = toHex(newColor)
        }

        binding.lightSliders.saturation.addOnChangeListener { _, value, _ ->
            hsl[1] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.lightAccent.value = toHex(newColor)
        }

        binding.lightSliders.lightness.addOnChangeListener { _, value, _ ->
            hsl[2] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.lightAccent.value = toHex(newColor)
        }
    }

    private fun editAccentDark() {

        var newColor: Int
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(colorDark, hsl)

        binding.darkSliders.hue.addOnChangeListener { _, value, _ ->
            hsl[0] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.darkAccent.value = toHex(newColor)
        }

        binding.darkSliders.saturation.addOnChangeListener { _, value, _ ->
            hsl[1] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.darkAccent.value = toHex(newColor)
        }

        binding.darkSliders.lightness.addOnChangeListener { _, value, _ ->
            hsl[2] = value
            newColor = ColorUtils.HSLToColor(hsl)
            model.darkAccent.value = toHex(newColor)
        }
    }
}
