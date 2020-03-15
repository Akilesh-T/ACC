package app.akilesh.qacc.ui.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class StyledSwitchPreferenceCompat(context: Context, attrs: AttributeSet) :
    SwitchPreferenceCompat(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val switchCompat = holder?.findViewById(R.id.switchWidget) as SwitchCompat
            switchCompat.trackDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.switch_track_material, context.theme)
            if (switchCompat.isChecked) {
                switchCompat.thumbTintList = ColorStateList.valueOf(context.getColorAccent())
                switchCompat.trackTintList = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        context.getColorAccent(),
                        127
                    )
                )
            } else {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
                val disabledColor = ColorUtils.setAlphaComponent(typedValue.data, 127)
                switchCompat.thumbTintList = ColorStateList.valueOf(typedValue.data)
                switchCompat.trackTintList = ColorStateList.valueOf(disabledColor)
            }
        }
    }
}