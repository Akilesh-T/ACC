package app.akilesh.qacc.ui.colorpicker.colorspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.akilesh.qacc.databinding.ColorPickerHslBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import codes.side.andcolorpicker.converter.setFromColorInt
import codes.side.andcolorpicker.converter.toOpaqueColorInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar

class HSLColorPicker : Fragment() {

    private lateinit var binding: ColorPickerHslBinding
    private lateinit var pickerGroup: PickerGroup<IntegerHSLColor>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorPickerHslBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pickerGroup = PickerGroup<IntegerHSLColor>().also {
            it.registerPickers(
                binding.hue, binding.saturation, binding.lightness
            )
        }

        val selectionObserver = Observer<Pair<Int, Boolean>> { pair ->
            pair?.let {
                if (it.second) setPickerColor(it.first)
            }
        }
        viewModel.selectedColor.observe(viewLifecycleOwner, selectionObserver)

        pickerGroup.addListener(
            object : HSLColorPickerSeekBar.OnColorPickListener {
                override fun onColorChanged(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int
                ) {
                }

                override fun onColorPicked(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onColorPicking(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) viewModel.selectColor(color.toOpaqueColorInt(), false)
                }
            }
        )

        if (viewModel.selectedColor.value == null) {
            val systemAccent = requireContext().getColorAccent()
            setPickerColor(systemAccent)
        }
    }

    private fun setPickerColor(
        color: Int
    ) {
        pickerGroup.setColor(
            IntegerHSLColor().also {
                it.setFromColorInt(color)
            }
        )
    }

    companion object {
        private lateinit var viewModel: ColorSpaceViewModel
        fun initViewModel(colorSpaceViewModel: ColorSpaceViewModel) {
            viewModel = colorSpaceViewModel
        }
    }
}