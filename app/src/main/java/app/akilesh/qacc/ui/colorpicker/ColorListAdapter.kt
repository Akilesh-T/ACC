package app.akilesh.qacc.ui.colorpicker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.RecyclerviewItemColorPreviewBinding
import app.akilesh.qacc.model.Colour

class ColorListAdapter internal constructor(
    private val context: Context,
    private val colours: List<Colour>,
    val adapterOnClick : (Colour) -> Unit
) : RecyclerView.Adapter<ColorListAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewItemColorPreviewBinding.inflate(layoutInflater, parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) = holder.bind(colours[position])

    inner class ColorViewHolder(private var binding: RecyclerviewItemColorPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(colour: Colour) {
            val backgroundColor = Color.parseColor(colour.hex)
            val textColor = Palette.Swatch(backgroundColor, 1).bodyTextColor
            binding.colorCard.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            binding.colorName.text = String.format(context.resources.getString(R.string.colour), colour.name, colour.hex)
            binding.colorName.setTextColor(textColor)
            binding.colorCard.setOnClickListener {  adapterOnClick(colour) }
        }
    }

    override fun getItemCount() = colours.size
}
