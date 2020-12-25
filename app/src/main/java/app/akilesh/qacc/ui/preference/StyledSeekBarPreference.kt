package app.akilesh.qacc.ui.preference

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class StyledSeekBarPreference(context: Context, attrs: AttributeSet) :
        SeekBarPreference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val seekBar = holder?.findViewById(R.id.seekbar) as SeekBar
            seekBar.apply {
                val colorStateList = ColorStateList.valueOf(context.getColorAccent())
                thumbTintList = colorStateList
                progressTintList = colorStateList
                progressBackgroundTintList = colorStateList
            }
        }
    }
}
