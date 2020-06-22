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

class InfoItemAdapter internal constructor(
    val infoItems: List<Info>,
    val useSystemAccent: Boolean,
    val systemAccent: Int
) : RecyclerView.Adapter<InfoItemAdapter.InfoItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SubitemAboutBinding.inflate(layoutInflater, parent, false)
        return InfoItemViewHolder(binding)
    }

    override fun getItemCount(): Int = infoItems.size

    override fun onBindViewHolder(holder: InfoItemViewHolder, position: Int) = holder.bind(position)

    inner class InfoItemViewHolder(private var binding: SubitemAboutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            if (useSystemAccent) {
               TextViewCompat.setCompoundDrawableTintList(binding.aboutSubItem, ColorStateList.valueOf(systemAccent))
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
    }
}