package app.akilesh.qacc.ui.colorpicker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Colors.mdColorPalette
import app.akilesh.qacc.databinding.ColorPickerFragmentBinding
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.setPreview

class MDColorsViewPagerAdapter internal constructor(
    private val viewModel: ColorPickerViewModel,
    private val colorPickerFragmentBinding: ColorPickerFragmentBinding,
    private val dialog: AlertDialog
) : RecyclerView.Adapter<MDColorsViewPagerAdapter.MDColorViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MDColorsViewPagerAdapter.MDColorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ColorPreviewBinding.inflate(layoutInflater, parent, false)
        return MDColorViewHolder(binding)
    }

    override fun getItemCount(): Int = mdColorPalette.count()

    override fun onBindViewHolder(holder: MDColorsViewPagerAdapter.MDColorViewHolder, position: Int) = holder.bind(position)

    inner class MDColorViewHolder(private var binding: ColorPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val colorListAdapter = ColorListAdapter(
                mdColorPalette.getValue(position)
            ) { selectedColour ->
                viewModel.colour.hex = selectedColour.hex
                viewModel.colour.name = selectedColour.name
                colorPickerFragmentBinding.name.setText(selectedColour.name)
                setPreview(colorPickerFragmentBinding, Color.parseColor(selectedColour.hex))
                dialog.cancel()
            }

            binding.recyclerViewColor.apply {
                adapter = colorListAdapter
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
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
    }
}