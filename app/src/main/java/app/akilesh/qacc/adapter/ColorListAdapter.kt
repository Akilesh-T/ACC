package app.akilesh.qacc.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.model.Colour
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class ColorListAdapter internal constructor(
    private val context: Context,
    private val colours: List<Colour>,
    val adapterOnClick : (Colour) -> Unit
) : RecyclerView.Adapter<ColorListAdapter.ColorViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: MaterialTextView = itemView.findViewById(R.id.color_name)
        val cardView: MaterialCardView = itemView.findViewById(R.id.color_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item_color_preview, parent, false)
        return ColorViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val current = colours[position]
        val backgroundColor = Color.parseColor(current.hex)
        val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
        holder.cardView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        holder.textView.text = String.format(context.resources.getString(R.string.colour), current.name, current.hex)
        holder.textView.setTextColor(textColor)
        holder.cardView.setOnClickListener {  adapterOnClick(current) }
    }

    override fun getItemCount() = colours.size

}