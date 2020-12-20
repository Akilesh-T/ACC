package app.akilesh.qacc.ui.info

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.AppInfoBinding
import app.akilesh.qacc.databinding.ItemAboutBinding
import app.akilesh.qacc.model.Info

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class InfoAdapter(
    private val infoItems: Map<Info, List<Info.InfoItem>?>,
    private val accentColor: Pair<Boolean, Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> InfoViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType: $viewType")
        }
    }

    override fun getItemCount(): Int = infoItems.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is InfoViewHolder -> {
                @Suppress("UNCHECKED_CAST")
                holder.bind(
                    position,
                    accentColor,
                    infoItems as Map<Info.InfoItem, List<Info.InfoItem>>
                )
            }
            is HeaderViewHolder -> {
                holder.bindHeader()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(infoItems.keys.elementAt(position)) {
            is Info.Header -> ITEM_VIEW_TYPE_HEADER
            is Info.InfoItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class InfoViewHolder private constructor(val binding: ItemAboutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            position: Int,
            accentColor: Pair<Boolean, Int>,
            infoItems: Map<Info.InfoItem, List<Info.InfoItem>>
        ) {
            if (accentColor.first) {
                val textColor = Palette.Swatch(accentColor.second, 1).bodyTextColor
                binding.cardTitle.apply {
                    setTextColor(textColor)
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(textColor))
                    backgroundTintList = ColorStateList.valueOf(accentColor.second)
                }
            }

            val key = infoItems.keys.elementAt(position)
            binding.cardTitle.apply {
                text = key.name
                setCompoundDrawablesRelativeWithIntrinsicBounds(key.drawableRes!!, 0, 0, 0)
            }
            binding.aboutItemRv.adapter = InfoItemAdapter(infoItems.getValue(key), accentColor)
        }

        companion object {
            fun from(parent: ViewGroup): InfoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAboutBinding.inflate(layoutInflater, parent, false)
                return InfoViewHolder(binding)
            }
        }
    }

    class HeaderViewHolder private constructor(val binding: AppInfoBinding)
        : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context
        private val packageName: String = context.packageName

        fun bindHeader() {
            val appInfoIntent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setData(Uri.parse("package:$packageName"))
            binding.root.setOnClickListener {
                context.startActivity(appInfoIntent)
            }

            val versionName = context.packageManager.getPackageInfo(packageName, 0).versionName
            binding.appVersion.text = context.getString(R.string.version, versionName)
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AppInfoBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }
}
