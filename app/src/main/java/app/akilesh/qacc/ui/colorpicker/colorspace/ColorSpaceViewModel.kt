package app.akilesh.qacc.ui.colorpicker.colorspace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColorSpaceViewModel : ViewModel() {

    val selectedColor = MutableLiveData<Int>()
    fun selectColor(color: Int) {
        selectedColor.value = color
    }

}