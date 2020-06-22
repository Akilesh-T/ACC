package app.akilesh.qacc.ui.info

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.databinding.ItemAboutBinding
import app.akilesh.qacc.model.Info

class InfoAdapter internal constructor(
    val infoItems: Map<Info, List<Info>>,
    val useSystemAccent: Boolean,
    val systemAccent: Int
) : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAboutBinding.inflate(layoutInflater, parent, false)
        return InfoViewHolder(binding)
    }

    override fun getItemCount(): Int = infoItems.count()

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) = holder.bind(position)

    inner class InfoViewHolder(private var binding: ItemAboutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            if (useSystemAccent) {
                val textColor = Palette.Swatch(systemAccent, 1).bodyTextColor
                binding.cardTitle.apply {
                    setTextColor(textColor)
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(textColor))
                    backgroundTintList = ColorStateList.valueOf(systemAccent)
                }
            }

            val key = infoItems.keys.elementAt(position)
            binding.cardTitle.apply {
                text = key.name
                setCompoundDrawablesRelativeWithIntrinsicBounds(key.drawableRes!!, 0, 0, 0)
            }
            binding.aboutItemRv.adapter = InfoItemAdapter(infoItems.getValue(key), useSystemAccent, systemAccent)
        }
    }
}
