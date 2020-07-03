package app.akilesh.qacc.ui.colorpicker.colorspace

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.akilesh.qacc.Const.Colors.colorSpaces

class ColorSpaceAdapter(
    fragment: Fragment,
    private val viewModel: ColorSpaceViewModel
): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = colorSpaces.size

    private lateinit var colorPicker: Fragment
    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> colorPicker = RGBColorPicker(viewModel)
            1 -> colorPicker = HSLColorPicker(viewModel)
            //2 -> colorPicker = LABColorPicker(viewModel)
        }
        return colorPicker
    }
}
