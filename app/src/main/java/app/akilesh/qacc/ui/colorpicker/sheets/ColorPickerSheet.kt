package app.akilesh.qacc.ui.colorpicker.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.akilesh.qacc.Const.Colors.colorList
import app.akilesh.qacc.Const.Colors.selectedColor
import app.akilesh.qacc.databinding.SheetColorPreviewBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.colorpicker.ColorListAdapter
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ColorPickerSheet: BottomSheetDialogFragment() {

    private lateinit var binding: SheetColorPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetColorPreviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val previousSavedStateHandle = navController.previousBackStackEntry?.savedStateHandle
        val list = previousSavedStateHandle?.get<List<Colour>>(colorList)

        val colorListAdapter = list?.let {
            ColorListAdapter(
                it
            ) { colour ->
                previousSavedStateHandle.set(selectedColor, colour)
                dismiss()
            }
        }

        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(requireContext())

        val useSystemAccent = sharedPreferences
            .getBoolean("system_accent", false)

        binding.recyclerViewColor.apply {

            layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )

            setHasFixedSize(true)

            adapter = colorListAdapter

            if (useSystemAccent) {
                edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                        return EdgeEffect(view.context).apply {
                            color = requireContext().getColorAccent()
                        }
                    }
                }
            }
        }
    }
}