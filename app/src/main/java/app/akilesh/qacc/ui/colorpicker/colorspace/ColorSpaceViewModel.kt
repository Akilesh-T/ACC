package app.akilesh.qacc.ui.colorpicker.colorspace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColorSpaceViewModel : ViewModel() {

    val selectedColor = MutableLiveData<Pair<Int, Boolean>>()
    fun selectColor(color: Int, observe: Boolean = true) {
        selectedColor.value = Pair(color, observe)
    }

}