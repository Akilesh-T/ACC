package app.akilesh.qacc.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.isOOS
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorCustomisationFragmentBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.createAccent
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.viewmodel.AccentViewModel
import app.akilesh.qacc.viewmodel.CustomisationViewModel
import com.topjohnwu.superuser.Shell
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

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        separateAccents = sharedPreferences.getBoolean("separate_accent", false)

        var accentLight = args.lightAccent
        var accentDark = args.darkAccent
        colorLight = Color.parseColor(accentLight)
        setPreviewLight(colorLight, accentLight)
        model = ViewModelProvider(this).get(CustomisationViewModel::class.java)
        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)

        val lightAccentObserver = Observer<String> {
            accentLight = it
            colorLight = Color.parseColor(accentLight)
            setPreviewLight(colorLight, accentLight)
        }
        model.lightAccent.observe(viewLifecycleOwner, lightAccentObserver)

        if (!separateAccents) {
            binding.chipGroup.visibility = View.GONE
            binding.previewDark.root.visibility = View.GONE
            updateColor(false)

        }
        else {
            colorDark = Color.parseColor(accentDark)
            setPreviewDark(colorDark, accentDark)

            val darkAccentObserver = Observer<String> {
                accentDark = it
                colorDark = Color.parseColor(accentDark)
                setPreviewDark(colorDark, accentDark)
            }
            model.darkAccent.observe(viewLifecycleOwner, darkAccentObserver)
        }

        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                binding.lightChip.id -> {
                    updateColor( false)
                }
                binding.darkChip.id -> {
                    updateColor( true)
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

            if (isOOS) {
                val result = Shell.su("settings put system oem_black_mode_accent_color \'$accentLight\'")
                    .exec()
                if (result.isSuccess) {
                    showSnackbar(view, "$accentLight set")
                    findNavController().navigate(R.id.back_home)
                }
            }

            var suffix = "hex_" + accentLight.removePrefix("#")
            val dark: String
            if (separateAccents) {
                suffix += "_" + accentDark.removePrefix("#")
                dark = accentDark
            }
            else dark = accentLight
            val pkgName = prefix + suffix
            val accent = Accent(pkgName, args.accentName, accentLight, dark)
            Log.d("accent", accent.toString())
            if (createAccent(context!!, accentViewModel, accent)) {
                showSnackbar(view, "${args.accentName} created")
                findNavController().navigate(R.id.to_home)
            }
        }

    }

    private fun setPreviewLight(color: Int, hex: String) {
        val colorName = if (separateAccents) context!!.resources.getString(R.string.light) else args.accentName
        binding.previewLight.colorName.text = String.format(context!!.resources.getString(R.string.colour), colorName, hex)
        val textColorLight = Palette.Swatch(color, 1).bodyTextColor
        binding.previewLight.colorName.setTextColor(textColorLight)
        binding.previewLight.colorCard.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun setPreviewDark(color: Int, hex: String) {
        binding.previewDark.colorName.text = String.format(context!!.resources.getString(R.string.colour), context!!.resources.getString(R.string.dark), hex)
        val textColorDark = Palette.Swatch(color, 1).bodyTextColor
        binding.previewDark.colorName.setTextColor(textColorDark)
        binding.previewDark.colorCard.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun updateColor(isDark: Boolean) {

        var newColor: Int
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(if (isDark) colorDark else colorLight, hsl)

        binding.hue.setOnChangeListener { _, value ->
            hsl[0] = value
            newColor = ColorUtils.HSLToColor(hsl)
            if (isDark)
                model.darkAccent.value = toHex(newColor)
            else
                model.lightAccent.value = toHex(newColor)
        }

        binding.saturation.setOnChangeListener { _, value ->
            hsl[1] = value
            newColor = ColorUtils.HSLToColor(hsl)
            if (isDark)
                model.darkAccent.value = toHex(newColor)
            else
                model.lightAccent.value = toHex(newColor)
        }

        binding.lightness.setOnChangeListener { _, value ->
            hsl[2] = value
            newColor = ColorUtils.HSLToColor(hsl)
            if (isDark)
                model.darkAccent.value = toHex(newColor)
            else
                model.lightAccent.value = toHex(newColor)
        }
    }
}