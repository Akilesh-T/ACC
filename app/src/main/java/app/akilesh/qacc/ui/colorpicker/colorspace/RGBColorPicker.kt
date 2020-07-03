package app.akilesh.qacc.ui.colorpicker.colorspace

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.akilesh.qacc.databinding.ColorPickerRgbBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import codes.side.andcolorpicker.converter.getBInt
import codes.side.andcolorpicker.converter.getGInt
import codes.side.andcolorpicker.converter.getRInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.model.IntegerRGBColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar

class RGBColorPicker(val viewModel: ColorSpaceViewModel) : Fragment() {

    private lateinit var binding: ColorPickerRgbBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorPickerRgbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pickerGroup = binding.rgbSet.pickerGroup

        val selectionObserver = Observer<Int> { colorInt ->
            colorInt?.let {
                setPickerColor(pickerGroup, it)
            }
        }
        viewModel.selectedColor.observe(viewLifecycleOwner, selectionObserver)

        pickerGroup.addListener(
            object :
                ColorSeekBar.OnColorPickListener<ColorSeekBar<IntegerRGBColor>, IntegerRGBColor> {
                override fun onColorChanged(
                    picker: ColorSeekBar<IntegerRGBColor>,
                    color: IntegerRGBColor,
                    value: Int
                ) {
                }

                override fun onColorPicked(
                    picker: ColorSeekBar<IntegerRGBColor>,
                    color: IntegerRGBColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onColorPicking(
                    picker: ColorSeekBar<IntegerRGBColor>,
                    color: IntegerRGBColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        /*
                    Not yet implemented
                    color.toOpaqueColorInt()
                    */
                        val colorInt = Color.rgb(color.getRInt(), color.getGInt(), color.getBInt())
                        viewModel.selectColor(colorInt)
                    }
                }
            }
        )

        if (viewModel.selectedColor.value == null) {
            val systemAccent = requireContext().getColorAccent()
            setPickerColor(pickerGroup, systemAccent)
        }
    }

    private fun setPickerColor(
        pickerGroup: PickerGroup<IntegerRGBColor>,
        color: Int
    ) {
        pickerGroup.setColor(
            IntegerRGBColor().also {
                it.intR = Color.red(color)
                it.intG = Color.green(color)
                it.intB = Color.blue(color)
                it.intA = 255
            }
        )
    }
}