package app.akilesh.qacc.ui.fragments

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.navAnim

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val accentColor = requireContext().getColorAccent()
        val colorStateList = ColorStateList.valueOf(accentColor)

        val themePref= findPreference<ListPreference>("themePref")
        val accentPref = findPreference<SwitchPreferenceCompat>("system_accent")
        val separateAccentPref = findPreference<SwitchPreferenceCompat>("separate_accent")
        val tweakPref = findPreference<SwitchPreferenceCompat>("customise")
        val backupPref = findPreference<Preference>("backups")
        val createAllPref = findPreference<Preference>("create_all")

        val preferences = listOf(
            themePref, accentPref, tweakPref, separateAccentPref, backupPref, createAllPref
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        if (useSystemAccent) {
            preferences.forEach {
                it?.icon?.setTintList(colorStateList)
            }
        }
        else {
            preferences.forEach {
                it?.icon?.setTintList(ColorStateList.valueOf(
                    ResourcesCompat.getColor(resources, R.color.colorPrimary, requireContext().theme)
                ))
            }
        }

        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            AppUtils.applyTheme(theme)
            true
        }

        if (SDK_INT < Q)
            separateAccentPref?.isVisible = false

        backupPref?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.backup_fragment, null, navAnim)
            true
        }

        accentPref?.setOnPreferenceChangeListener { _, _ ->
            requireActivity().recreate()
            true
        }

        createAllPref?.setOnPreferenceClickListener {
           /* val createAllViewModel = ViewModelProvider(this).get(CreateAllViewModel::class.java)
            createAllViewModel.createAll()*/
            findNavController().navigate(R.id.create_all_fragment, null, navAnim)
            true
        }
    }
}

