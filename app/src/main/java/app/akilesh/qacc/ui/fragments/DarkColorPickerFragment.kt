package app.akilesh.qacc.ui.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.akilesh.qacc.Const
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.adapter.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.createAccent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
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
import kotlin.properties.Delegates

class DarkColorPickerFragment: Fragment() {

    private var accentColor = ""
    private var accentName = ""
    private var previewColor by Delegates.notNull<Int>()
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

        previewColor = if (accentColor.isBlank()) requireContext().getColorAccent()
        else Color.parseColor(accentColor)
        setPreview(binding, previewColor)

        val accentColorLight = args.lightAccent
        accentViewModel = ViewModelProvider(this).get(AccentViewModel::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val customise = sharedPreferences.getBoolean("customise", false)
        binding.title.text = String.format(getString(R.string.picker_title_text_dark))

        if (customise) binding.buttonNext.text = requireContext().resources.getString(R.string.next)


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
                    if (createAccent(requireContext(), accentViewModel, accent)) {
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

        binding.brandColors.setOnClickListener { chooseFromPresets(R.string.brand_colors, R.drawable.ic_palette_24dp,
            Const.Colors.brandColors
        ) }
        binding.custom.setOnClickListener { setCustomColor() }
        binding.preset.setOnClickListener { chooseFromPresets(R.string.presets, R.drawable.ic_preset,
            Const.Colors.AEX
        ) }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            binding.wallColors.setOnClickListener { chooseFromWallpaperColors() }
        else
            binding.wallColors.visibility = View.GONE

        binding.name.doAfterTextChanged {
            accentName = it.toString().trim()
        }

    }

    private fun setCustomColor() {

        ChromaDialog.Builder()
            .initialColor(previewColor)
            .colorMode(ColorMode.ARGB)
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

    private fun chooseFromPresets(@StringRes title: Int, @DrawableRes icon: Int, colorList: List<Colour>) {

        val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
        val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
        dialogTitleBinding.titleText.text = String.format(resources.getString(title))
        dialogTitleBinding.titleIcon.setImageResource(icon)
        val builder = MaterialAlertDialogBuilder(context)
            .setCustomTitle(dialogTitleBinding.root)
            .setView(colorPreviewBinding.root)
        val dialog = builder.create()

        val adapter = ColorListAdapter(requireContext(), colorList) { colour ->
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


    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun chooseFromWallpaperColors() {

        val rationaleHandler = createDialogRationale(R.string.app_name) {
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
                val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
                val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
                dialogTitleBinding.titleText.text = String.format(resources.getString(R.string.color_wallpaper))
                dialogTitleBinding.titleIcon.setImageResource(R.drawable.ic_wallpaper)
                val builder = MaterialAlertDialogBuilder(context)
                    .setCustomTitle(dialogTitleBinding.root)
                    .setView(colorPreviewBinding.root)
                val dialog = builder.create()

                val adapter = ColorListAdapter(requireContext(), requireContext().getWallpaperColors()) { colour ->
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