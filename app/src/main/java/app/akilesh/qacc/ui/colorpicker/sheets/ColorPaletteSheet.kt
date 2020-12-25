package app.akilesh.qacc.ui.colorpicker.sheets

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.Colors.mdColorPalette
import app.akilesh.qacc.Const.Colors.selectedColor
import app.akilesh.qacc.databinding.SheetMdColorPaletteBinding
import app.akilesh.qacc.ui.colorpicker.MDColorsViewPagerAdapter
import app.akilesh.qacc.ui.colorpicker.ZoomOutPageTransformer
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ColorPaletteSheet: BottomSheetDialogFragment() {

    private lateinit var binding: SheetMdColorPaletteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetMdColorPaletteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pager.apply {
            setPageTransformer(ZoomOutPageTransformer())
            adapter =
                MDColorsViewPagerAdapter {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        selectedColor, it
                    )
                    dismiss()
                }
        }
        setTabIndicatorColor(binding.tabLayout, 0)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, _ ->
            tab.view.isClickable = false
        }.attach()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent && SDK_INT >= Q)
            binding.tabLayout.setEdgeEffectColor(requireContext().getColorAccent())

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tab?.position
                    setTabIndicatorColor(binding.tabLayout, position)
                }
            }
        )
    }

    private fun setTabIndicatorColor(tabLayout: TabLayout, position: Int?) {
        val color = Color.parseColor(mdColorPalette[position]?.get(7)?.hex)
        tabLayout.apply {
            setSelectedTabIndicatorColor(color)
            tabRippleColor = ColorStateList.valueOf(color)
        }
    }
}