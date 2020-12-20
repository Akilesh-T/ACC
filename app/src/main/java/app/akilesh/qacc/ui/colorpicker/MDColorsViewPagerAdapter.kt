package app.akilesh.qacc.ui.colorpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Colors.mdColorPalette
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.utils.AppUtils.getColorAccent

class MDColorsViewPagerAdapter internal constructor(
    private val itemOnClick : (Colour) -> Unit
): RecyclerView.Adapter<MDColorsViewPagerAdapter.MDColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MDColorViewHolder.from(parent)

    override fun getItemCount(): Int = mdColorPalette.count()

    override fun onBindViewHolder(holder: MDColorViewHolder, position: Int) =
        holder.bind(position, itemOnClick)

    class MDColorViewHolder(val binding: ColorPreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, itemOnClick: (Colour) -> Unit) {
            val colorListAdapter = ColorListAdapter(
                mdColorPalette.getValue(position)
            ) { selectedColour ->
                itemOnClick(selectedColour)
            }

            binding.handle.visibility = View.GONE
            binding.recyclerViewColor.apply {
                adapter = colorListAdapter
                layoutManager = GridLayoutManager(
                    context,
                    2,
                    GridLayoutManager.VERTICAL,
                    false
                )
                setHasFixedSize(true)

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
                if (useSystemAccent) {
                    edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                            return EdgeEffect(view.context).apply {
                                color = context.getColorAccent()
                            }
                        }
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): MDColorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ColorPreviewBinding.inflate(layoutInflater, parent, false)
                return MDColorViewHolder(binding)
            }
        }
    }
}