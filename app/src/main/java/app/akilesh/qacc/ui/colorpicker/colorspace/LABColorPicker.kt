package app.akilesh.qacc.ui.colorpicker.colorspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.akilesh.qacc.databinding.ColorPickerLabBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import codes.side.andcolorpicker.converter.toColorInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.lab.LABColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerLABColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar

class LABColorPicker : Fragment() {

    private lateinit var binding: ColorPickerLabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorPickerLabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pickerGroup = PickerGroup<IntegerLABColor>().also {
            it.registerPickers(
                binding.l, binding.a, binding.b
            )
        }

        val selectionObserver = Observer<Pair<Int, Boolean>> { pair ->
            pair?.let {
                //setPickerColor(pickerGroup, it)
            }
        }
        viewModel.selectedColor.observe(viewLifecycleOwner, selectionObserver)

        pickerGroup.addListener(
            object : LABColorPickerSeekBar.OnColorPickListener {
                override fun onColorChanged(
                    picker: ColorSeekBar<IntegerLABColor>,
                    color: IntegerLABColor,
                    value: Int
                ) {
                }

                override fun onColorPicked(
                    picker: ColorSeekBar<IntegerLABColor>,
                    color: IntegerLABColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onColorPicking(
                    picker: ColorSeekBar<IntegerLABColor>,
                    color: IntegerLABColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                    /*
                     Not yet implemented
                     color.toOpaqueColorInt()
                                      */
                    if (fromUser)
                        viewModel.selectColor(color.toColorInt())
                }
            }
        )

        if (viewModel.selectedColor.value == null) {
            val systemAccent = requireContext().getColorAccent()
            //setPickerColor(pickerGroup, systemAccent)
        }
    }

    /*
    Not yet implemented

    private fun setPickerColor(
        pickerGroup: PickerGroup<IntegerLABColor>,
        color: Int
    ) {
        pickerGroup.setColor(
            IntegerLABColor().also {
                it.setFromColorInt(color)
            }
        )
    }
*/

    companion object {
        private lateinit var viewModel: ColorSpaceViewModel
        fun initViewModel(colorSpaceViewModel: ColorSpaceViewModel) {
            viewModel = colorSpaceViewModel
        }
    }
}