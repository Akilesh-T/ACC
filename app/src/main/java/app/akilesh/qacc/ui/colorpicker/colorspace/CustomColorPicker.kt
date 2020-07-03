package app.akilesh.qacc.ui.colorpicker.colorspace

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import app.akilesh.qacc.Const.Colors.colorSpaces
import app.akilesh.qacc.databinding.CustomColorPickerBinding
import app.akilesh.qacc.ui.colorpicker.ZoomOutPageTransformer
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.toHex
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText

interface CustomColorPicker {

    var dialog: AlertDialog
    val colorSpaceViewModel: ColorSpaceViewModel
    val fragment: Fragment

    fun showCustomColorPicker(
        context: Context,
        customColorPickerBinding: CustomColorPickerBinding
    ) {
        customColorPickerBinding.pager.apply {
            setPageTransformer(ZoomOutPageTransformer())
            isUserInputEnabled = false
            adapter = ColorSpaceAdapter(fragment, colorSpaceViewModel)
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        val systemAccent = context.getColorAccent()
        if (useSystemAccent) {
            customColorPickerBinding.tabLayout.apply {
                setSelectedTabIndicatorColor(systemAccent)
                setTabTextColors(tabTextColors!!.defaultColor, systemAccent)
                tabRippleColor = ColorStateList.valueOf(systemAccent)
            }
        }

        setupTextInputLayout(
            customColorPickerBinding,
            if (colorSpaceViewModel.selectedColor.value != null) colorSpaceViewModel.selectedColor.value!!
            else systemAccent,
            fragment.viewLifecycleOwner
        )

        TabLayoutMediator(customColorPickerBinding.tabLayout, customColorPickerBinding.pager) { tab, position ->
            tab.text = colorSpaces[position]
        }.attach()
        dialog.show()

        if (useSystemAccent) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                isAllCaps = false
                setTextColor(systemAccent)
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                isAllCaps = false
                setTextColor(systemAccent)
            }
        }
    }

    private fun setupTextInputLayout(
        customColorPickerBinding: CustomColorPickerBinding,
        color: Int,
        viewLifecycleOwner: LifecycleOwner
    ) {
        val hexPrefix = customColorPickerBinding.hexInput.prefixText.toString()
        val textWatcher = customColorPickerBinding.hex.addTextChangedListener { text ->
            val hex = hexPrefix + text
            if (text?.length == 6) {
                colorSpaceViewModel.selectColor(Color.parseColor(hex))
            }
        }

        customColorPickerBinding.hex.apply {
            updateText(toHex(color).removePrefix(hexPrefix), textWatcher)
            setPreviewColor(customColorPickerBinding, color)
        }

        val selectionObserver = Observer<Int> { colorInt ->
            colorInt?.let {
                customColorPickerBinding.hex.apply {
                    updateText(toHex(it).removePrefix(hexPrefix), textWatcher)
                    setPreviewColor(customColorPickerBinding, it)
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
        customColorPickerBinding: CustomColorPickerBinding,
        color: Int
    ) {
        customColorPickerBinding.apply {
            preview.backgroundTintList = ColorStateList.valueOf(color)
            val colorStateList = ColorStateList.valueOf(Palette.Swatch(color, 1).bodyTextColor)
            hexInput.apply {
                setBoxStrokeColorStateList(colorStateList)
                setPrefixTextColor(colorStateList)
                setEndIconTintList(colorStateList)
                counterTextColor = colorStateList
            }
            hex.apply {
                setTextColor(colorStateList)
                if (SDK_INT >= Q) {
                    textCursorDrawable?.setTintList(colorStateList)
                    textSelectHandle?.setTintList(colorStateList)
                    textSelectHandleLeft?.setTintList(colorStateList)
                    textSelectHandleRight?.setTintList(colorStateList)
                }
            }
        }
    }
}