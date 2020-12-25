package app.akilesh.qacc.ui.preference

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
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
            val colorStateList =  ColorStateList.valueOf(context.getColorAccent())
            if (switchCompat.isChecked) {
                switchCompat.thumbTintList = colorStateList
                switchCompat.trackTintList = colorStateList.withAlpha(127)
            }
        }
    }
}