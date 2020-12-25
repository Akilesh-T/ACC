package app.akilesh.qacc.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class StyledPreferenceCategory(context: Context, attrs: AttributeSet?) :
    PreferenceCategory(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            val title = holder.findViewById(android.R.id.title) as TextView
            title.setTextColor(context.getColorAccent())
        }
    }
}