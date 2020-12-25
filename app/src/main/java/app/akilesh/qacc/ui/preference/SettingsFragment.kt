package app.akilesh.qacc.ui.preference

import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils.applyTheme
import app.akilesh.qacc.utils.AppUtils.getColorAccent
import app.akilesh.qacc.utils.AppUtils.getThemeColor
import app.akilesh.qacc.utils.AppUtils.navAnim

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val themePref= findPreference<ListPreference>("themePref")
        val accentPref = findPreference<SwitchPreferenceCompat>("system_accent")
        val separateAccentPref = findPreference<SwitchPreferenceCompat>("separate_accent")
        val backupPref = findPreference<Preference>("backups")
        val createAllPref = findPreference<Preference>("create_all")
        val autoBackupPref = findPreference<SwitchPreferenceCompat>("auto_backup")
        val autoBackupIntervalPref = findPreference<SeekBarPreference>("auto_backup_interval")
        val deleteOld = findPreference<SwitchPreferenceCompat>("delete_old")
        val dailyAccentPref = findPreference<SwitchPreferenceCompat>("daily_accent")

        val preferences = listOf(
            themePref, accentPref, separateAccentPref, backupPref,
            createAllPref, autoBackupPref, autoBackupIntervalPref, deleteOld, dailyAccentPref
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useSystemAccent = sharedPreferences.getBoolean("system_accent", false)

        val iconTint = if (useSystemAccent) ColorStateList.valueOf(requireContext().getColorAccent())
        else ColorStateList.valueOf(requireContext().getThemeColor(R.attr.colorPrimary))
        preferences.forEach { preference ->
            preference?.icon?.setTintList(iconTint)
        }

        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            applyTheme(theme)
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

        val viewModel: SettingsViewModel by viewModels()
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
        dailyAccentPref?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean)
                viewModel.enableDailyAccentSwitcher()
            else viewModel.disableDailyAccentSwitcher()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       /*
        Sometimes bottom app bar hides the lower portion of the screen.
        Add some padding to the bottom and don't clip to padding.
        */
        listView.setPaddingRelative(
            listView.paddingStart,
            listView.paddingTop,
            listView.paddingEnd,
            listView.paddingBottom + 120
        )
        listView.clipToPadding = false
    }
}

