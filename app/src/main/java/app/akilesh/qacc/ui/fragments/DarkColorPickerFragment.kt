package app.akilesh.qacc.ui.fragments

import android.annotation.SuppressLint
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.akilesh.qacc.Const
import app.akilesh.qacc.R
import app.akilesh.qacc.ui.adapter.ColorListAdapter
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
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
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener

class DarkColorPickerFragment: Fragment() {

    private var accentColor = ""
    private var accentName = ""
    private lateinit var binding: ColorPickerFragmentBinding
    private lateinit var accentViewModel: AccentViewModel
    private val args: DarkColorPickerFragmentArgs by navArgs()

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

        val accentColorLight = args.lightAccent
        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val customise = sharedPreferences.getBoolean("customise", false)
        binding.title.text = String.format(getString(R.string.picker_title_text_dark))

        if (customise) binding.buttonNext.text = context!!.resources.getString(R.string.next)


        binding.buttonNext.setOnClickListener {

            if (customise) {
                if (accentName.isNotBlank() && accentColor.isNotBlank()) {
                    val action =
                        DarkColorPickerFragmentDirections.toCustomise(accentColorLight, accentColor, accentName)
                    findNavController().navigate(action)
                } else {
                    if (accentColor.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_color_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (accentName.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_name_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

                if (accentColor.isNotBlank() && accentName.isNotBlank()) {
                    val suffix = "hex_" + accentColorLight.removePrefix("#")
                    val pkgName = Const.prefix + suffix
                    val accent = Accent(pkgName, accentName, accentColorLight, accentColor)
                    Log.d("accent", accent.toString())
                    if (createAccent(context!!, accentViewModel, accent)) {
                        showSnackbar(view, String.format(getString(R.string.accent_created), accentName))
                        findNavController().navigate(R.id.back_home)
                    }
                } else {
                    if (accentColor.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_color_not_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (accentName.isBlank()) Toast.makeText(
                        context,
                        getString(R.string.toast_name_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        }

        binding.buttonPrevious.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.custom.setOnClickListener { setCustomColor() }
        binding.preset.setOnClickListener { chooseFromPresets() }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
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

        val adapter = ColorListAdapter(context!!, Const.Colors.presets) { colour ->
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


    @SuppressLint("MissingPermission")
    private fun chooseFromWallpaperColors() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

            val rationaleHandler = createDialogRationale(R.string.app_name_full) {
                onPermission(
                    Permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.storage_permission_rationale)
                )
            }

            runWithPermissions(
                Permission.READ_EXTERNAL_STORAGE,
                rationaleHandler = rationaleHandler
            ) {
                if (it.isAllGranted()) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val wallDrawable = wallpaperManager.drawable
                    var wallColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)!!

                    val colorsChangedListener = WallpaperManager.OnColorsChangedListener { colors, _ ->
                        wallColors = colors ?: WallpaperColors.fromDrawable(wallDrawable)
                    }
                    wallpaperManager.addOnColorsChangedListener(colorsChangedListener, Handler())

                    val primary = wallColors.primaryColor.toArgb()
                    val secondary = wallColors.secondaryColor?.toArgb()
                    val tertiary = wallColors.tertiaryColor?.toArgb()

                    val primaryHex = toHex(primary)
                    val wallpaperColours = mutableListOf(Colour(primaryHex, getString(R.string.wallpaper_primary)))
                    if (secondary != null) {
                        val secondaryHex = toHex(secondary)
                        wallpaperColours.add(Colour(secondaryHex, getString(R.string.wallpaer_secondary)))
                    }
                    if (tertiary != null) {
                        val tertiaryHex = toHex(tertiary)
                        wallpaperColours.add(Colour(tertiaryHex, getString(R.string.wallpaper_tertiary)))
                    }

                    if (wallpaperManager.wallpaperInfo == null) {

                        val bitmap = BitmapFactory.decodeFileDescriptor(
                            wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM).fileDescriptor
                        )
                        val palette = Palette.from(bitmap).generate()

                        val defaultColor =
                            ResourcesCompat.getColor(resources, android.R.color.transparent, null)
                        val vibrant = palette.getVibrantColor(defaultColor)
                        if (vibrant != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(vibrant),
                                "Vibrant"
                            )
                        )

                        val darkVibrant = palette.getDarkVibrantColor(defaultColor)
                        if (darkVibrant != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(
                                    darkVibrant
                                ), "Dark Vibrant"
                            )
                        )

                        val lightVibrant = palette.getLightVibrantColor(defaultColor)
                        if (lightVibrant != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(
                                    lightVibrant
                                ), "Light Vibrant"
                            )
                        )

                        val muted = palette.getMutedColor(defaultColor)
                        if (muted != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(muted),
                                "Muted"
                            )
                        )

                        val darkMuted = palette.getDarkMutedColor(defaultColor)
                        if (darkMuted != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(darkMuted),
                                "Dark Muted"
                            )
                        )

                        val lightMuted = palette.getLightMutedColor(defaultColor)
                        if (lightMuted != defaultColor) wallpaperColours.add(
                            Colour(
                                toHex(lightMuted),
                                "Light Muted"
                            )
                        )
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