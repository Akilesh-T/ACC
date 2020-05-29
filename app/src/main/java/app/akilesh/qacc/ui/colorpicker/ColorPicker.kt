package app.akilesh.qacc.ui.colorpicker

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.databinding.DialogTitleBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.AppUtils.setPreview
import app.akilesh.qacc.utils.AppUtils.toHex
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        }

        dialog.show()
    }
}
