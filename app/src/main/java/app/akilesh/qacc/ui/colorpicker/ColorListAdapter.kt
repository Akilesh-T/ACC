package app.akilesh.qacc.ui.colorpicker

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ItemColorPreviewBinding
import app.akilesh.qacc.model.Colour

class ColorListAdapter internal constructor(
    private val colours: List<Colour>,
    val adapterOnClick : (Colour) -> Unit
) : RecyclerView.Adapter<ColorListAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemColorPreviewBinding.inflate(layoutInflater, parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) =
        holder.bind(colours[position])

    inner class ColorViewHolder(val binding: ItemColorPreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(colour: Colour) {
            val backgroundColor = Color.parseColor(colour.hex)
            val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
            binding.colorCard.apply {
                backgroundTintList = ColorStateList.valueOf(backgroundColor)
                setOnClickListener {
                    adapterOnClick(colours[bindingAdapterPosition])
                }
            }
            binding.colorName.apply {
                text = context.resources.getString(
                    R.string.colour,
                    colour.name,
                    colour.hex
                )
                setTextColor(textColor)
            }
        }
    }

    override fun getItemCount() = colours.size
}
