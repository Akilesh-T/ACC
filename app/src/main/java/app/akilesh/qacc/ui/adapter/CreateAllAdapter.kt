package app.akilesh.qacc.ui.adapter

import android.content.Context
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
import app.akilesh.qacc.model.Colour
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView

class CreateAllAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<CreateAllAdapter.CreateAllViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var tracker: SelectionTracker<Long>? = null
    var colours: MutableList<Colour> = mutableListOf()
    init {
        setHasStableIds(true)
    }

    inner class CreateAllViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: MaterialTextView = itemView.findViewById(R.id.color_name)
        val cardView: MaterialCardView = itemView.findViewById(R.id.color_card)
        val selection: MaterialCheckBox = itemView.findViewById(R.id.selection)

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateAllAdapter.CreateAllViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item_color_preview, parent, false)
        return CreateAllViewHolder(itemView)
    }

    override fun getItemCount(): Int = colours.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: CreateAllAdapter.CreateAllViewHolder, position: Int) {
        val current = colours[position]
        val backgroundColor = Color.parseColor(current.hex)
        val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
        holder.cardView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        holder.textView.visibility = View.GONE

        holder.selection.apply {
            visibility = View.VISIBLE
            text = String.format(context.resources.getString(R.string.colour), current.name, current.hex)
            setTextColor(textColor)
            buttonTintList = ColorStateList.valueOf(Palette.Swatch(backgroundColor, 1).titleTextColor)
            isChecked = tracker?.isSelected(position.toLong())!!
        }
    }

}