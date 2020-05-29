package app.akilesh.qacc.ui.createmultiple

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.RecyclerviewItemColorPreviewBinding
import app.akilesh.qacc.model.Colour

class CreateMultipleAdapter internal constructor() : RecyclerView.Adapter<CreateMultipleAdapter.CreateAllViewHolder>() {

    var tracker: SelectionTracker<Long>? = null
    var colours: MutableList<Colour> = mutableListOf()
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateAllViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewItemColorPreviewBinding.inflate(layoutInflater, parent, false)
        return CreateAllViewHolder(binding)
    }

    override fun getItemCount(): Int = colours.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: CreateAllViewHolder, position: Int) = holder.bind(position)

    inner class CreateAllViewHolder(private var binding: RecyclerviewItemColorPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }

        fun bind(position: Int)  {
            val colour = colours[position]
            val backgroundColor = Color.parseColor(colour.hex)
            val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
            binding.colorCard.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            binding.colorName.visibility = View.GONE

            binding.selection.apply {
                visibility = View.VISIBLE
                text = String.format(context.resources.getString(R.string.colour), colour.name, colour.hex)
                setTextColor(textColor)
                buttonTintList = ColorStateList.valueOf(Palette.Swatch(backgroundColor, 1).titleTextColor)
                isChecked = tracker?.isSelected(position.toLong())!!
            }
        }
    }
}
