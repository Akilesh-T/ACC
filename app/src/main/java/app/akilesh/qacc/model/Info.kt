package app.akilesh.qacc.model

import androidx.annotation.DrawableRes

data class Info (
    val name: String,
    @DrawableRes val drawableRes: Int?,
    val link: String?
)