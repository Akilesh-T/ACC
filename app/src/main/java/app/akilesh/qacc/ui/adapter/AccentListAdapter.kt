package app.akilesh.qacc.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.topjohnwu.superuser.Shell

class AccentListAdapter internal constructor(
    private val context: Context,
    val edit: (Accent) -> Unit,
    val uninstall: (Accent) -> Unit
): RecyclerView.Adapter<AccentListAdapter.AccentViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var accents = mutableListOf<Accent>()

    inner class AccentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: MaterialTextView = itemView.findViewById(R.id.color_name)
        val lightAccent: MaterialTextView = itemView.findViewById(R.id.light_accent)
        val darkAccent: MaterialTextView = itemView.findViewById(R.id.dark_accent)
        val card: MaterialCardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item_accents, parent, false)
        return AccentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) {

        val current = accents[position]
        val colorLight = Color.parseColor(current.colorLight)
        val isInstalled = isOverlayInstalled(current.pkgName)

        holder.name.text = current.name
        if (isInstalled.not()) holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_indeterminate, 0)
        if (current.colorDark.isNotBlank() && current.colorDark != current.colorLight) {
            holder.darkAccent.text = current.colorDark
            val colorDark = Color.parseColor(current.colorDark)
            TextViewCompat.setCompoundDrawableTintList(holder.darkAccent, ColorStateList.valueOf(colorDark))
            holder.lightAccent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_outline_wb_sunny, 0, 0, 0)
        }
        else {
            holder.darkAccent.visibility = View.GONE
        }
        holder.lightAccent.text = current.colorLight
        TextViewCompat.setCompoundDrawableTintList(holder.lightAccent, ColorStateList.valueOf(colorLight))


        val popupMenu = PopupMenu(context, holder.itemView)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        if (SDK_INT >= Q) popupMenu.setForceShowIcon(true)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.edit -> edit(current)
                R.id.uninstall -> {
                    if (isOverlayEnabled(current.pkgName)) disableAccent(current.pkgName)
                    uninstall(current)
                }
            }
            true
        }
        holder.card.setOnLongClickListener {
            popupMenu.show()
            true
        }

        if (isInstalled) {
            val isEnabled = isOverlayEnabled(current.pkgName)
            holder.card.apply {
                if (isEnabled) {
                   setCardBackground(holder, context.getColorAccent())
                }
            }
            holder.card.setOnClickListener {
                if (isEnabled) disableAccent(current.pkgName)
                else enableAccent(current.pkgName)
            }
        }
    }

    private fun setCardBackground(
        holder: AccentViewHolder,
        colorAccent: Int
    ) {
        val colorStateList = ColorStateList.valueOf(colorAccent)
        val textColor = Palette.Swatch(colorAccent, 2).bodyTextColor
        holder.apply {
            card.backgroundTintList = colorStateList
            name.setTextColor(textColor)
            lightAccent.apply {
                setTextColor(textColor)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(textColor))
            }
            if (darkAccent.isVisible) {
                darkAccent.apply {
                    setTextColor(textColor)
                    TextViewCompat.setCompoundDrawableTintList(
                        this,
                        ColorStateList.valueOf(textColor)
                    )
                }
            }
        }
    }

    internal fun setAccents(accents: MutableList<Accent>) {
        this.accents = accents
        notifyDataSetChanged()
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

    private fun enableAccent(packageName: String) {
        accents.forEach{ accent ->
            if (isOverlayInstalled(accent.pkgName) && isOverlayEnabled(accent.pkgName)) disableAccent(accent.pkgName)
        }
        Shell.su(
            "cmd overlay enable $packageName",
            "cmd overlay set-priority $packageName highest"
        ).submit()
        //Shell.su("cmd overlay enable-exclusive --category ${current.pkgName}").exec()
    }

    private fun disableAccent(packageName: String) {
        Shell.su("cmd overlay disable $packageName").exec()
    }
}
