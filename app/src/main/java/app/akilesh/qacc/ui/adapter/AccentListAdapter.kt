package app.akilesh.qacc.ui.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import com.topjohnwu.superuser.Shell

class AccentListAdapter internal constructor(
    private val context: Context
): RecyclerView.Adapter<AccentListAdapter.AccentViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var accents = mutableListOf<Accent>()


    inner class AccentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardView)
        val name: MaterialTextView = itemView.findViewById(R.id.name)
        val color: AppCompatImageView = itemView.findViewById(R.id.color)
        val box: MaterialCheckBox = itemView.findViewById(R.id.enable_or_disable_accent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return AccentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) {

        val current = accents[position]
        val colorLight = Color.parseColor(current.colorLight)
        var text = current.name + " - " + current.colorLight
        if (current.colorDark.isNotBlank() && current.colorDark != current.colorLight) text +=  " & " + current.colorDark
        holder.name.text = text
        holder.color.setColorFilter(colorLight)

        if (isOverlayInstalled(current.pkgName)) {

            if (isOverlayEnabled(current.pkgName)) {
                holder.box.isChecked = true
                val accentColor = context.getColorAccent()
                holder.color.setColorFilter(accentColor)
                holder.card.strokeWidth = 3
                holder.card.strokeColor = accentColor

            }
            else holder.box.isChecked = false

            holder.box.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    accents.forEach {
                        Shell.su("cmd overlay disable ${it.pkgName}").exec()
                    }
                    Shell.su(
                        "cmd overlay enable ${current.pkgName}",
                        "cmd overlay set-priority ${current.pkgName} highest"
                    ).exec()
                    //Shell.su("cmd overlay enable-exclusive --category ${current.pkgName}").exec()
                }
                else {
                    Shell.su("cmd overlay disable ${current.pkgName}").exec()
                }
            }
        }
        else {
            holder.box.hint = "Not installed"
            holder.box.isClickable = false
            holder.box.buttonDrawable = null
        }
    }

    internal fun setAccents(accents: MutableList<Accent>) {
        this.accents = accents
        notifyDataSetChanged()
    }

    internal fun getAccentAndRemoveAt(position: Int): Accent {
        val current = accents[position]
        accents.removeAt(position)
        notifyItemRemoved(position)
        return current
    }
    override fun getItemCount() = accents.size

    private fun isOverlayInstalled(pkgName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(pkgName, 0).enabled
        } catch (e: Exception) {
            false
        }
    }

    private fun isOverlayEnabled(pkgName: String): Boolean {
        var isEnabled = false
        if (SDK_INT == Q)
            isEnabled = Shell.su("cmd overlay dump isenabled $pkgName").exec().out.component1() == "true"
        else {
            val overlays = Shell.su("cmd overlay list").exec().out
            overlays.forEach {
                if (it.startsWith("[x]") && it.contains(pkgName))
                    isEnabled = true
            }
        }
        return isEnabled
    }
}