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
import app.akilesh.qacc.databinding.RecyclerviewItemAccentsBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.topjohnwu.superuser.Shell

class AccentListAdapter internal constructor(
    private val context: Context,
    val edit: (Accent) -> Unit,
    val uninstall: (Accent) -> Unit
): RecyclerView.Adapter<AccentListAdapter.AccentViewHolder>() {
    private var accents = mutableListOf<Accent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewItemAccentsBinding.inflate(layoutInflater, parent, false)
        return AccentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) = holder.bind(accents[position])

    inner class AccentViewHolder(private var binding: RecyclerviewItemAccentsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(accent: Accent) {
            val colorLight = Color.parseColor(accent.colorLight)
            val isInstalled = isOverlayInstalled(accent.pkgName)

            binding.colorName.text = accent.name
            if (isInstalled.not()) binding.colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_outline_indeterminate, 0)
            if (accent.colorDark.isNotBlank() && accent.colorDark != accent.colorLight) {
                binding.darkAccent.text = accent.colorDark
                val colorDark = Color.parseColor(accent.colorDark)
                TextViewCompat.setCompoundDrawableTintList(binding.darkAccent, ColorStateList.valueOf(colorDark))
                binding.lightAccent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_outline_wb_sunny, 0, 0, 0)
            }
            else {
                binding.darkAccent.visibility = View.GONE
            }
            binding.lightAccent.text = accent.colorLight
            TextViewCompat.setCompoundDrawableTintList(binding.lightAccent, ColorStateList.valueOf(colorLight))


            val popupMenu = PopupMenu(context, binding.root)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            if (SDK_INT >= Q) popupMenu.setForceShowIcon(true)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.edit -> edit(accent)
                    R.id.uninstall -> {
                        if (isOverlayEnabled(accent.pkgName)) disableAccent(accent.pkgName)
                        uninstall(accent)
                    }
                }
                true
            }
            binding.cardView.setOnLongClickListener {
                popupMenu.show()
                true
            }

            if (isInstalled) {
                val isEnabled = isOverlayEnabled(accent.pkgName)
                binding.cardView.apply {
                    if (isEnabled) {
                        setCardBackground(binding, context.getColorAccent())
                    }
                }
                binding.cardView.setOnClickListener {
                    if (isEnabled) disableAccent(accent.pkgName)
                    else enableAccent(accent.pkgName)
                }
            }
        }
    }

    private fun setCardBackground(
        binding: RecyclerviewItemAccentsBinding,
        colorAccent: Int
    ) {
        val colorStateList = ColorStateList.valueOf(colorAccent)
        val textColor = Palette.Swatch(colorAccent, 2).bodyTextColor
        binding.apply {
            cardView.backgroundTintList = colorStateList
            colorName.setTextColor(textColor)
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
