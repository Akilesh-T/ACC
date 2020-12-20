package app.akilesh.qacc.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.OverlayUtils.isOverlayEnabled
import app.akilesh.qacc.utils.OverlayUtils.isOverlayInstalled
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("drawable")
fun MaterialTextView.setDrawable(hasNativeDarkMode: Boolean) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        if (hasNativeDarkMode) R.drawable.ic_outline_wb_sunny else R.drawable.ic_palette_24dp,
        0, 0, 0
    )
}

@BindingAdapter("drawableTint")
fun MaterialTextView.setDrawableTint(
    hex: String
) {
    val textColorStateList = ColorStateList.valueOf(Color.parseColor(hex))
    TextViewCompat.setCompoundDrawableTintList(
        this,
        textColorStateList
    )
}

@BindingAdapter("isChecked")
fun MaterialCardView.isChecked(pkgName: String) {
    isChecked = isOverlayEnabled(pkgName)
    if (isChecked) {
        val checkedTint = ColorStateList.valueOf(context.getColorAccent())
        checkedIconTint = checkedTint
        foregroundTintList = checkedTint
    }
}

@BindingAdapter("isEnabled")
fun MaterialCardView.isEnabled(pkgName: String) {
    val isInstalled = context.packageManager.isOverlayInstalled(pkgName)
    //isEnabled = isInstalled
    if (isInstalled.not()) setCardForegroundColor(cardBackgroundColor.withAlpha(192))
}
