package app.akilesh.qacc.ui.colorpicker.sheets

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O_MR1
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.navigation.navGraphViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Colors.AEX
import app.akilesh.qacc.Const.Colors.brandColors
import app.akilesh.qacc.Const.Colors.brandColorsArg
import app.akilesh.qacc.Const.Colors.listArg
import app.akilesh.qacc.Const.Colors.presetsArg
import app.akilesh.qacc.Const.Colors.wallpaperColorsArg
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.ColorPreviewBinding
import app.akilesh.qacc.model.Accent
import app.akilesh.qacc.ui.colorpicker.ColorListAdapter
import app.akilesh.qacc.ui.colorpicker.ColorPickerViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getWallpaperColors
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ColorPickerSheet: BottomSheetDialogFragment() {

    private lateinit var binding: ColorPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ColorPreviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = when(requireArguments().getString(listArg)) {
            presetsArg -> AEX
            brandColorsArg -> brandColors
            wallpaperColorsArg -> if (SDK_INT >= O_MR1) {
                requireContext().getWallpaperColors()
            } else listOf()
            else -> listOf()
        }
        val isDark = requireArguments().getBoolean("isDark")
        val viewModel: ColorPickerViewModel by navGraphViewModels(R.id.nav_graph)
        val colorListAdapter = ColorListAdapter(
            list
        ) { selectedColour ->
            if (isDark)
                viewModel.accent.value = Accent(
                    String(),
                    selectedColour.name,
                    viewModel.accent.value!!.colorLight,
                    selectedColour.hex
                )
            else
                viewModel.accent.value = Accent(
                    String(),
                    selectedColour.name,
                    selectedColour.hex,
                    String()
                )
            dismiss()
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