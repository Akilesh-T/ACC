package app.akilesh.qacc.viewmodel

import androidx.lifecycle.ViewModel
import app.akilesh.qacc.model.Colour

class ColorPickerViewModel : ViewModel() {
    val colour = Colour("", "")
    var accentLight = ""
}
