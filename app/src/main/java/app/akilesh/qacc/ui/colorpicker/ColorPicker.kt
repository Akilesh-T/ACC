package app.akilesh.qacc.ui.colorpicker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.view.LayoutInflater
import android.widget.EdgeEffect
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Colors.mdColorPalette
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.databinding.MdColorPaletteBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.toHex
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener

interface ColorPicker {

    var binding: ColorPickerFragmentBinding
    val viewModel: ColorPickerViewModel

    fun customColorPicker(previewColor: Int, parentFragmentManager: FragmentManager) {
        ChromaDialog.Builder()
            .initialColor(previewColor)
            .colorMode(ColorMode.RGB)
            .onColorSelected(object : ColorSelectListener {
                override fun onColorSelected(color: Int) {
                    viewModel.colour.hex = toHex(color)
                    setPreview(binding, color)
                    binding.name.text = null
                }
            })
            .create()
            .show(parentFragmentManager, "ChromaDialog")
    }

    fun showColorPickerDialog(context: Context, layoutInflater: LayoutInflater, @StringRes title: Int, @DrawableRes icon: Int, colorList: List<Colour>) {

        val colorPreviewBinding = ColorPreviewBinding.inflate(layoutInflater)
        val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
        dialogTitleBinding.apply {
            titleText.text = String.format(context.resources.getString(title))
            titleIcon.setImageDrawable(ResourcesCompat.getDrawable(context.resources, icon, null))
        }
        val builder = MaterialAlertDialogBuilder(context)
            .setCustomTitle(dialogTitleBinding.root)
            .setView(colorPreviewBinding.root)
        val dialog = builder.create()

        val colorListAdapter = ColorListAdapter(
            context,
            colorList
        ) { selectedColour ->
            viewModel.colour.hex = selectedColour.hex
            viewModel.colour.name = selectedColour.name
            binding.name.setText(selectedColour.name)
            setPreview(binding, Color.parseColor(selectedColour.hex))
            dialog.cancel()
        }

        colorPreviewBinding.recyclerViewColor.apply {
            adapter = colorListAdapter
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
            if (useSystemAccent) {
                edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                        return EdgeEffect(view.context).apply {
                            color = context.getColorAccent()
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    fun showTabDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        binding: ColorPickerFragmentBinding
    ) {
        val mdColorPaletteBinding = MdColorPaletteBinding.inflate(layoutInflater)
        val dialogTitleBinding = DialogTitleBinding.inflate(layoutInflater)
        dialogTitleBinding.apply {
            titleText.text = String.format(context.resources.getString(R.string.md_colors))
            titleIcon.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_palette_24dp, null))
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setCustomTitle(dialogTitleBinding.root)
            .setView(mdColorPaletteBinding.root)
            .create()

        mdColorPaletteBinding.pager.apply {
            setPageTransformer(ZoomOutPageTransformer())
            adapter = MDColorsViewPagerAdapter(context, viewModel, binding, dialog)
        }
        setTabIndicatorColor(mdColorPaletteBinding.tabLayout, 0)
        TabLayoutMediator(mdColorPaletteBinding.tabLayout, mdColorPaletteBinding.pager) { _, _ ->
        }.attach()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent && SDK_INT >= Q) mdColorPaletteBinding.tabLayout.setEdgeEffectColor(context.getColorAccent())
        mdColorPaletteBinding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tab?.position
                    setTabIndicatorColor(mdColorPaletteBinding.tabLayout, position)
                }
            }
        )
        dialog.show()
    }

    private fun setTabIndicatorColor(tabLayout: TabLayout, position: Int?) {
        val color = Color.parseColor(mdColorPalette[position]?.get(7)?.hex)
        tabLayout.apply {
            setSelectedTabIndicatorColor(color)
            tabRippleColor = ColorStateList.valueOf(color)
        }
    }
}
