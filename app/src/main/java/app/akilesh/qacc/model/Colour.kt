package app.akilesh.qacc.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Colour(
    var hex: String,
    var name: String
) : Parcelable