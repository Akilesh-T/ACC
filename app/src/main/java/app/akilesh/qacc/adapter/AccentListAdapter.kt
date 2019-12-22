package app.akilesh.qacc.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.topjohnwu.superuser.Shell

class AccentListAdapter internal constructor(
    private val context: Context
): RecyclerView.Adapter<AccentListAdapter.AccentViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var accents = mutableListOf<Accent>()


    inner class AccentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: MaterialTextView = itemView.findViewById(R.id.name)
        val color: AppCompatImageView = itemView.findViewById(R.id.color)
        val button: MaterialButton = itemView.findViewById(R.id.enable_or_disable_accent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return AccentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) {
        val current = accents[position]
        val text = current.name + " - " + current.color
        val color = Color.parseColor(current.color)
        val colorStateList = ColorStateList.valueOf(color)
        holder.name.text = text
        holder.color.setColorFilter(color)

        if (isOverlayInstalled(current.pkgName)) {

            if(isOverlayEnabled(current.pkgName)) {
                holder.button.strokeColor = colorStateList
                holder.button.strokeWidth = 3
                holder.button.text = context.resources.getString(R.string.disable_accent)
                holder.button.icon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_check_circle_unchecked, context.theme)
            }
            else {
                holder.button.text = context.resources.getString(R.string.enable_accent)
                holder.button.icon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_check_circle_checked, context.theme)
            }

            holder.button.iconTint = colorStateList
            holder.button.rippleColor = colorStateList
            holder.button.setOnClickListener {
                if (holder.button.text == "Enable") {
                    //Disable accents created by the app and enable selected accent
                    accents.forEach {
                        Shell.su("cmd overlay disable ${it.pkgName}").exec()
                    }
                    Shell.su(
                        "cmd overlay enable ${current.pkgName}",
                        "cmd overlay set-priority ${current.pkgName} highest"
                        ).exec()
                    //Shell.su("cmd overlay enable-exclusive --category ${current.pkgName}").exec()
                }
                if (holder.button.text == "Disable") {
                    //Disable enabled accent
                    Shell.su("cmd overlay disable ${current.pkgName}").exec()
                }
            }
        }
        else {
            holder.button.strokeWidth = 0
            holder.button.text = context.resources.getString(R.string.overlay_not_installed)
            holder.button.isClickable = false
            holder.button.icon = null
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
            context.packageManager.getPackageInfo(pkgName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isOverlayEnabled(pkgName: String): Boolean {
        return Shell.su("cmd overlay dump isenabled $pkgName").exec().out.component1() == "true"
    }
}