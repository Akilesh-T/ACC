package app.akilesh.qacc.ui.colorpicker.sheets

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import app.akilesh.qacc.Const.Colors.colorSpaces
import app.akilesh.qacc.Const.Colors.customHex
import app.akilesh.qacc.Const.Colors.editDark
import app.akilesh.qacc.Const.Colors.editLight
import app.akilesh.qacc.Const.Colors.editType
import app.akilesh.qacc.Const.Colors.selectedColor
import app.akilesh.qacc.R
import app.akilesh.qacc.databinding.CustomColorPickerBinding
import app.akilesh.qacc.model.Colour
import app.akilesh.qacc.ui.colorpicker.ZoomOutPageTransformer
import app.akilesh.qacc.ui.colorpicker.colorspace.ColorSpaceAdapter
import app.akilesh.qacc.ui.colorpicker.colorspace.ColorSpaceViewModel
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.toHex
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText

class CustomColorPicker : BottomSheetDialogFragment() {

    private lateinit var binding: CustomColorPickerBinding
    private val colorSpaceViewModel: ColorSpaceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CustomColorPickerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.pager.apply {
            setPageTransformer(ZoomOutPageTransformer())
            isUserInputEnabled = false
            adapter = ColorSpaceAdapter(this@CustomColorPicker, colorSpaceViewModel)
        }

        val previousStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

        val prevHex = previousStateHandle?.get<String>(customHex)
        val color = if (prevHex != null && prevHex.isNotBlank()) Color.parseColor(prevHex)
        else requireContext().getColorAccent()

        colorSpaceViewModel.selectColor(color)
        setupTextInputLayout(color)

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = colorSpaces[position]
        }.attach()

        binding.cancel.setOnClickListener { dismiss() }
        binding.ok.setOnClickListener {
            colorSpaceViewModel.selectedColor.value?.let {
                val hex = toHex(it.first)

                // Edit
                when(previousStateHandle?.get<String>(editType)) {
                    editLight -> previousStateHandle.set(editLight, hex)
                    editDark -> previousStateHandle.set(editDark, hex)
                }

                // Select
                previousStateHandle?.set(
                    selectedColor, Colour(hex, getString(R.string.color_custom))
                )

                dismiss()
            }
        }
    }

    private fun setupTextInputLayout(
        color: Int
    ) {
        val hexPrefix = binding.hexInput.prefixText.toString()
        val textWatcher = binding.hex.addTextChangedListener { text ->
            val hex = hexPrefix + text
            if (text?.length == 6) {
                colorSpaceViewModel.selectColor(Color.parseColor(hex))
            }
        }

        binding.hex.apply {
            updateText(toHex(color).removePrefix(hexPrefix), textWatcher)
            setPreviewColor(color)
        }

        val selectionObserver = Observer<Pair<Int, Boolean>> { pair ->
            pair?.let {
                binding.hex.apply {
                    updateText(toHex(it.first).removePrefix(hexPrefix), textWatcher)
                    setPreviewColor(it.first)
                }
            }
        }
        colorSpaceViewModel.selectedColor.observe(viewLifecycleOwner, selectionObserver)
    }

    private fun TextInputEditText.updateText(
        text: String,
        textWatcher: TextWatcher
    ) {
        removeTextChangedListener(textWatcher)
        setText(text)
        addTextChangedListener(textWatcher)
    }

    private fun setPreviewColor(
        color: Int
    ) {
        val colorStateList = ColorStateList.valueOf(color)
        binding.apply {
            preview.backgroundTintList = colorStateList

            val colorOnPreview = ColorStateList.valueOf(Palette.Swatch(color, 1).bodyTextColor)

            hexInput.apply {
                setBoxStrokeColorStateList(colorOnPreview)
                setPrefixTextColor(colorOnPreview)
                setEndIconTintList(colorOnPreview)
                counterTextColor = colorOnPreview
            }

            hex.apply {
                setTextColor(colorOnPreview)
                if (SDK_INT >= Q) {
                    textCursorDrawable?.setTintList(colorOnPreview)
                    textSelectHandle?.setTintList(colorOnPreview)
                    textSelectHandleLeft?.setTintList(colorOnPreview)
                    textSelectHandleRight?.setTintList(colorOnPreview)
                }
            }

            cancel.apply {
                setTextColor(color)
                rippleColor = colorOnPreview
            }
            ok.backgroundTintList = colorStateList

            tabLayout.apply {
                setSelectedTabIndicatorColor(color)
                setTabTextColors(tabTextColors!!.defaultColor, color)
                tabRippleColor = colorStateList
            }
        }
    }
}
