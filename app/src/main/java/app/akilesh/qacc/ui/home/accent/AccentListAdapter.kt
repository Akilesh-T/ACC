package app.akilesh.qacc.ui.home.accent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.databinding.ItemAccentsBinding
import app.akilesh.qacc.model.Accent

class AccentListAdapter internal constructor(
    private val listeners: ClickListeners
): PagingDataAdapter<Accent, AccentListAdapter.AccentViewHolder>(AccentDiffCallback) {

    override fun onBindViewHolder(holder: AccentViewHolder, position: Int) =
        holder.bind(getItem(position), listeners)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AccentViewHolder.from(parent)

    class AccentViewHolder private constructor(val binding: ItemAccentsBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nullableAccent: Accent?, listeners: ClickListeners) {
            nullableAccent?.let { accent ->
                binding.root.setOnLongClickListener { listeners.onLongClick(accent); true }
                binding.accent = accent
                binding.listener = listeners
                binding.executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): AccentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccentsBinding.inflate(
                    layoutInflater, parent, false
                )
                return AccentViewHolder(binding)
            }
        }
    }

    private object AccentDiffCallback : DiffUtil.ItemCallback<Accent>() {
        override fun areItemsTheSame(oldItem: Accent, newItem: Accent): Boolean {
            return oldItem.pkgName == newItem.pkgName
        }

        override fun areContentsTheSame(oldItem: Accent, newItem: Accent): Boolean {
            return oldItem == newItem
        }
    }

    class ClickListeners(
        val toggleListener: (String) -> Unit,
        val showInfo: (Accent) -> Unit
    ) {
        fun onClick(pkgName: String) = toggleListener(pkgName)
        fun onLongClick(accent: Accent) = showInfo(accent)
    }
}
