package app.akilesh.qacc.ui.info

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.databinding.SubitemAboutBinding
import app.akilesh.qacc.model.Info

class InfoItemAdapter(
    private val infoItems: List<Info.InfoItem>,
    private val accentColor: Pair<Boolean, Int>,
) : RecyclerView.Adapter<InfoItemAdapter.InfoItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoItemViewHolder
            = InfoItemViewHolder.from(parent)

    override fun getItemCount(): Int = infoItems.size

    override fun onBindViewHolder(holder: InfoItemViewHolder, position: Int)
            = holder.bind(position, infoItems, accentColor)

    class InfoItemViewHolder private constructor(val binding: SubitemAboutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, infoItems: List<Info.InfoItem>, accentColor: Pair<Boolean, Int>) {
            if (accentColor.first) {
               TextViewCompat.setCompoundDrawableTintList(
                   binding.aboutSubItem, ColorStateList.valueOf(accentColor.second)
               )
            }
            val infoItem = infoItems[position]
            binding.aboutSubItem.apply {
                text = infoItem.name
                infoItem.drawableRes?.let {
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this,
                        it, 0, 0, 0)
                }
                infoItem.link?.let { link -> setOnClickListener { context.openURL(link) } }
            }
        }

        private fun Context.openURL(url: String){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        companion object {
            fun from(parent: ViewGroup): InfoItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SubitemAboutBinding.inflate(layoutInflater, parent, false)
                return InfoItemViewHolder(binding)
            }
        }
    }
}