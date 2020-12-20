package app.akilesh.qacc.ui.createmultiple

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.RecyclerviewItemCreateMultipleBinding
import app.akilesh.qacc.model.Colour

class CreateMultipleAdapter internal constructor()
    : ListAdapter<Colour, CreateMultipleAdapter.CreateAllViewHolder>(ColourDiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateAllViewHolder
            = CreateAllViewHolder.from(parent)

    override fun onBindViewHolder(holder: CreateAllViewHolder, position: Int)
            = holder.bind(getItem(position))

    class CreateAllViewHolder private constructor(val binding: RecyclerviewItemCreateMultipleBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = itemId
                override fun inSelectionHotspot(e: MotionEvent): Boolean = true
            }

        fun bind(colour: Colour)  {
            val backgroundColor = Color.parseColor(colour.hex)
            val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
            binding.colorCard.backgroundTintList = ColorStateList.valueOf(backgroundColor)

            binding.selection.apply {
                text = String.format(context.resources.getString(R.string.colour), colour.name, colour.hex)
                setTextColor(textColor)
                buttonTintList = ColorStateList.valueOf(Palette.Swatch(backgroundColor, 1).titleTextColor)
                isChecked = tracker.isSelected(bindingAdapterPosition.toLong())
            }
        }

        companion object {
            fun from(parent: ViewGroup): CreateAllViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerviewItemCreateMultipleBinding.inflate(
                    layoutInflater, parent, false
                )
                return CreateAllViewHolder(binding)
            }
        }
    }

    private object ColourDiffCallback : DiffUtil.ItemCallback<Colour>() {
        override fun areItemsTheSame(oldItem: Colour, newItem: Colour): Boolean {
            return oldItem.hex == newItem.hex
        }

        override fun areContentsTheSame(oldItem: Colour, newItem: Colour): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private lateinit var tracker: SelectionTracker<Long>
        fun initTracker(selectionTracker: SelectionTracker<Long>) {
            tracker = selectionTracker
        }
    }
}
