package app.akilesh.qacc.ui.preferences

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
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
        val autoBackupPref = findPreference<SwitchPreferenceCompat>("auto_backup")
        val autoBackupIntervalPref = findPreference<SeekBarPreference>("auto_backup_interval")
        val deleteOld = findPreference<SwitchPreferenceCompat>("delete_old")

        val preferences = listOf(
            themePref, accentPref, tweakPref, separateAccentPref, backupPref, createAllPref, autoBackupPref, autoBackupIntervalPref, deleteOld
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)
        preferences.forEach { preference ->
            if (useSystemAccent) {
                preference?.icon?.setTintList(colorStateList)
            } else {
                preference?.icon?.setTintList(ColorStateList.valueOf(
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
            findNavController().navigate(R.id.create_all_fragment, null, navAnim)
            true
        }

        val viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        val backupInterval = sharedPreferences.getInt("auto_backup_interval", 15)
        autoBackupPref?.summaryOn = resources.getQuantityString(R.plurals.auto_backup_summary_on, backupInterval, backupInterval)
        autoBackupPref?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean)
                viewModel.enableAutoBackup(sharedPreferences.getInt("auto_backup_interval", 15).toLong())
            else viewModel.disableAutoBackup()
            true
        }
        autoBackupIntervalPref?.setOnPreferenceChangeListener { _, newValue ->
            val interval = newValue as Int
            autoBackupPref?.summaryOn = resources.getQuantityString(R.plurals.auto_backup_summary_on, interval, interval)
            viewModel.enableAutoBackup(interval.toLong())
            true
        }
    }
}

