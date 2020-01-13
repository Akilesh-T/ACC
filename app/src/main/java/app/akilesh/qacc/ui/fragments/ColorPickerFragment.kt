package app.akilesh.qacc.ui.fragments

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_SYSTEM
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.akilesh.qacc.Const.Colors.presets
import app.akilesh.qacc.Const.isOOS
import app.akilesh.qacc.Const.prefix
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.adapter.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.createAccent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.showSnackbar
import app.akilesh.qacc.utils.AppUtils.toHex
import app.akilesh.qacc.viewmodel.AccentViewModel
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener

class ColorPickerFragment: Fragment() {

    var accentColor = ""
    private var accentName = ""
    private lateinit var binding: ColorPickerFragmentBinding
    private lateinit var accentViewModel: AccentViewModel

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

        val systemAccentColor = this.context!!.getColorAccent()
        setPreview(binding, systemAccentColor)

        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var separateAccents = sharedPreferences.getBoolean("separate_accent", false)
        val customise = sharedPreferences.getBoolean("customise", false)
        if (SDK_INT < Q || isOOS) separateAccents = false

        if (separateAccents) {
            binding.title.text = String.format(
                context!!.resources.getString(R.string.picker_title_text),
                "for light theme"
            )
            binding.buttonNext.text = context!!.resources.getString(R.string.next)
            binding.textInputLayout.visibility = View.INVISIBLE
        }
        else
            binding.title.text = String.format(context!!.resources.getString(R.string.picker_title_text), "")

        if (customise)
            binding.buttonNext.text = context!!.resources.getString(R.string.next)

        binding.buttonNext.setOnClickListener {

            if (separateAccents) {
                if (accentColor.isNotBlank()) {
                    val action = ColorPickerFragmentDirections.toDark(accentColor)
                    findNavController().navigate(action)
                }
                else
                    Toast.makeText(context, "Accent color for light theme is not selected",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            else if (customise) {
                if (accentColor.isNotBlank() && accentName.isNotBlank()) {
                    val action = ColorPickerFragmentDirections.toCustomise(accentColor, accentName, accentColor)
                    findNavController().navigate(action)
                }
                else {
                    if (accentColor.isBlank()) Toast.makeText(
                        context,
                        "Accent color is not selected",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (accentName.isBlank()) Toast.makeText(
                        context,
                        "Accent name is not set",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else {
                if (isOOS) {
                    if (accentColor.isNotBlank()) {
                        val result = Shell.su("settings put system oem_black_mode_accent_color \'$accentColor\'")
                                .exec()
                        if (result.isSuccess) {
                            showSnackbar(view, "$accentColor set")
                            findNavController().navigate(R.id.back_home)
                        }
                    }
                    else
                        Toast.makeText(
                            context,
                            "Accent color is not selected",
                            Toast.LENGTH_SHORT
                        ).show()
                } else {
                    if (accentColor.isNotBlank() && accentName.isNotBlank()) {
                        val suffix = "hex_" + accentColor.removePrefix("#")
                        val pkgName = prefix + suffix
                        val accent = Accent(pkgName, accentName, accentColor, accentColor)
                        Log.d("accent", accent.toString())
                        if (createAccent(context!!, accentViewModel, accent)) {
                            showSnackbar(view, "$accentName created")
                            findNavController().navigate(R.id.back_home)
                        }
                    } else {
                        if (accentColor.isBlank()) Toast.makeText(
                            context,
                            "Accent color is not selected",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (accentName.isBlank()) Toast.makeText(
                            context,
                            "Accent name is not set",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.buttonPrevious.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.custom.setOnClickListener { setCustomColor() }
        binding.preset.setOnClickListener { chooseFromPresets() }
        if (SDK_INT > O)
            binding.wallColors.setOnClickListener { chooseFromWallpaperColors() }
        else
            binding.wallFrame.visibility = View.GONE

        binding.name.doAfterTextChanged {
            accentName = it.toString().trim()
        }

    }

    private fun setCustomColor() {

        ChromaDialog.Builder()
            .initialColor(Color.parseColor("#FF2800"))
            .colorMode(ColorMode.RGB)
            .onColorSelected(object : ColorSelectListener {
                override fun onColorSelected(color: Int) {
                    accentColor = toHex(color)
                    setPreview(binding, color)
                    binding.name.text = null
                }
            })
            .create()
            .show(parentFragmentManager, "ChromaDialog")

    }

    private fun chooseFromPresets() {

        val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
        val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
        dialogTitleBinding.titleText.text = String.format(resources.getString(R.string.presets))
        dialogTitleBinding.titleIcon.setImageResource(R.drawable.ic_preset)
        val builder = MaterialAlertDialogBuilder(context)
            .setCustomTitle(dialogTitleBinding.root)
            .setView(colorPreviewBinding.root)
        val dialog = builder.create()

        val adapter = ColorListAdapter(context!!, presets) { colour ->
            accentColor = colour.hex
            accentName = colour.name
            binding.name.setText(colour.name)
            setPreview(binding, Color.parseColor(accentColor))
            dialog.cancel()
        }

        colorPreviewBinding.recyclerViewColor.adapter = adapter
        colorPreviewBinding.recyclerViewColor.layoutManager = LinearLayoutManager(context)

        dialog.show()
    }


    private fun chooseFromWallpaperColors() {
        if (SDK_INT > O) {

            val rationaleHandler = createDialogRationale(R.string.app_name_full) {
                onPermission(
                    Permission.READ_EXTERNAL_STORAGE,
                    "Storage permission is required to get wallpaper colours."
                )
            }

            runWithPermissions(
                Permission.READ_EXTERNAL_STORAGE,
                rationaleHandler = rationaleHandler
            ) {
                if (it.isAllGranted()) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val wallDrawable = wallpaperManager.drawable
                    var wallColors = wallpaperManager.getWallpaperColors(FLAG_SYSTEM)!!

                    val colorsChangedListener = WallpaperManager.OnColorsChangedListener { colors, _ ->
                        wallColors = colors ?: WallpaperColors.fromDrawable(wallDrawable)
                    }
                    wallpaperManager.addOnColorsChangedListener(colorsChangedListener, Handler())

                    val primary = wallColors.primaryColor.toArgb()
                    val secondary = wallColors.secondaryColor?.toArgb()
                    val tertiary = wallColors.tertiaryColor?.toArgb()

                    val primaryHex = toHex(primary)
                    val wallpaperColours = mutableListOf(Colour(primaryHex, "Wallpaper primary"))
                    if (secondary != null) {
                        val secondaryHex = toHex(secondary)
                        wallpaperColours.add(Colour(secondaryHex, "Wallpaper secondary"))
                    }
                    if (tertiary != null) {
                        val tertiaryHex = toHex(tertiary)
                        wallpaperColours.add(Colour(tertiaryHex, "Wallpaper tertiary"))
                    }

                    val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
                    val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
                    dialogTitleBinding.titleText.text = String.format(resources.getString(R.string.color_wallpaper))
                    dialogTitleBinding.titleIcon.setImageResource(R.drawable.ic_wallpaper)
                    val builder = MaterialAlertDialogBuilder(context)
                        .setCustomTitle(dialogTitleBinding.root)
                        .setView(colorPreviewBinding.root)
                    val dialog = builder.create()

                    val adapter = ColorListAdapter(context!!, wallpaperColours) { colour ->
                        accentColor = colour.hex
                        accentName = colour.name
                        setPreview(binding, Color.parseColor(accentColor))
                        binding.name.text = null
                        dialog.cancel()
                    }

                    colorPreviewBinding.recyclerViewColor.adapter = adapter
                    colorPreviewBinding.recyclerViewColor.layoutManager = LinearLayoutManager(context)

                    dialog.show()
                }
            }
        }
    }
}