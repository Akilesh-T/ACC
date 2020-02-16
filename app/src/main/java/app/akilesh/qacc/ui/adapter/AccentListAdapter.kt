package app.akilesh.qacc.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.topjohnwu.superuser.Shell

class AccentListAdapter internal constructor(
    private val context: Context,
    val edit: (Accent) -> Unit
): RecyclerView.Adapter<AccentListAdapter.AccentViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var accents = mutableListOf<Accent>()


    inner class AccentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardView)
        val name: MaterialTextView = itemView.findViewById(R.id.color_name)
        val switchMaterial: SwitchMaterial = itemView.findViewById(R.id.enable_disable_accent)
        val lightAccent: MaterialTextView = itemView.findViewById(R.id.light_accent)
        val darkAccent: MaterialTextView = itemView.findViewById(R.id.dark_accent)
        val edit: MaterialButton = itemView.findViewById(R.id.edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item_accents, parent, false)
        return AccentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) {

        val current = accents[position]
        val colorLight = Color.parseColor(current.colorLight)
        val colorDark = Color.parseColor(current.colorDark)
        holder.edit.apply {
            rippleColor = ColorStateList.valueOf(colorLight)
            setOnClickListener {
                edit(current)
            }
        }

        holder.name.text = current.name
        holder.lightAccent.text = current.colorLight
        holder.lightAccent.compoundDrawableTintList = ColorStateList.valueOf(colorLight)
        if (current.colorDark.isNotBlank() && current.colorDark != current.colorLight) {
            holder.darkAccent.text = current.colorDark
            holder.darkAccent.compoundDrawableTintList = ColorStateList.valueOf(colorDark)
        }
        else {
            holder.darkAccent.visibility = View.GONE
        }

        if (isOverlayInstalled(current.pkgName)) {

            if (isOverlayEnabled(current.pkgName)) {
                val accentColor = context.getColorAccent()
                holder.switchMaterial.apply {
                    isChecked = true
                    thumbTintList = ColorStateList.valueOf(accentColor)
                    trackTintList =
                        ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 150))
                }
            }
            else holder.switchMaterial.isChecked = false

            holder.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
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
            holder.switchMaterial.apply {
                hint = "Not installed"
                isClickable = false
                thumbDrawable = null
                trackDrawable = null
            }
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