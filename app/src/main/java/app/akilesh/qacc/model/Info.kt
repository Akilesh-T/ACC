package app.akilesh.qacc.model

import androidx.annotation.DrawableRes

sealed class Info {

    data class InfoItem(
        val name: String,
        @DrawableRes val drawableRes: Int?,
        val link: String?
    ): Info()

    object Header: Info()
}