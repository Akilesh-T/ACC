package app.akilesh.qacc.ui.home.accent.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.databinding.ItemAccentInfoBinding
import app.akilesh.qacc.model.AccentInfo

class AccentInfoAdapter
    : ListAdapter<AccentInfo, AccentInfoAdapter.AccentInfoViewHolder>(AccentInfoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AccentInfoViewHolder.from(parent)

    override fun onBindViewHolder(holder: AccentInfoViewHolder, position: Int) =
        holder.bind(getItem(position))

    class AccentInfoViewHolder private constructor(val binding: ItemAccentInfoBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(accentInfo: AccentInfo) {
            binding.accentInfo = accentInfo
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AccentInfoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccentInfoBinding.inflate(
                    layoutInflater, parent, false
                )
                return AccentInfoViewHolder(binding)
            }
        }
    }

    private object AccentInfoDiffCallback : DiffUtil.ItemCallback<AccentInfo>() {
        override fun areItemsTheSame(oldItem: AccentInfo, newItem: AccentInfo): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: AccentInfo, newItem: AccentInfo): Boolean {
            return oldItem == newItem
        }
    }
}
