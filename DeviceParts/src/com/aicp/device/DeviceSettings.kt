/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2020 Android Ice Cold Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.aicp.device

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.TwoStatePreference
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.util.Log
import java.util.*

class DeviceSettings : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    private var mVibratorSystemStrength: VibratorSystemStrengthPreference? = null
    private var mVibratorCallStrength: VibratorCallStrengthPreference? = null
    private var mVibratorNotifStrength: VibratorNotifStrengthPreference? = null
    private var mSliderModeTop: ListPreference? = null
    private var mSliderModeCenter: ListPreference? = null
    private var mSliderModeBottom: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main, rootKey)
        mVibratorSystemStrength = findPreference(KEY_SYSTEM_VIBSTRENGTH) as VibratorSystemStrengthPreference?
        if (mVibratorSystemStrength != null) {
            mVibratorSystemStrength?.setEnabled(VibratorSystemStrengthPreference.isSupported)
        }
        mVibratorCallStrength = findPreference(KEY_CALL_VIBSTRENGTH) as VibratorCallStrengthPreference?
        if (mVibratorCallStrength != null) {
            mVibratorCallStrength?.setEnabled(VibratorCallStrengthPreference.isSupported)
        }
        mVibratorNotifStrength = findPreference(KEY_NOTIF_VIBSTRENGTH) as VibratorNotifStrengthPreference?
        if (mVibratorNotifStrength != null) {
            mVibratorNotifStrength?.setEnabled(VibratorNotifStrengthPreference.isSupported)
        }
        mSliderModeTop = findPreference(KEY_SLIDER_MODE_TOP) as ListPreference?
        mSliderModeTop!!.setOnPreferenceChangeListener(this)
        val sliderModeTop = getSliderAction(0)
        var valueIndex: Int = mSliderModeTop!!.findIndexOfValue(sliderModeTop.toString())

        mSliderModeTop!!.setValueIndex(valueIndex)
        mSliderModeTop!!.setSummary(mSliderModeTop!!.getEntries().get(valueIndex))
        mSliderModeCenter = findPreference(KEY_SLIDER_MODE_CENTER) as ListPreference?
        mSliderModeCenter!!.setOnPreferenceChangeListener(this)
        val sliderModeCenter = getSliderAction(1)
        valueIndex = mSliderModeCenter!!.findIndexOfValue(sliderModeCenter.toString())
        mSliderModeCenter!!.setValueIndex(valueIndex)
        mSliderModeCenter!!.setSummary(mSliderModeCenter!!.getEntries().get(valueIndex))
        mSliderModeBottom = findPreference(KEY_SLIDER_MODE_BOTTOM) as ListPreference?
        mSliderModeBottom!!.setOnPreferenceChangeListener(this)
        val sliderModeBottom = getSliderAction(2)
        valueIndex = mSliderModeBottom!!.findIndexOfValue(sliderModeBottom.toString())
        mSliderModeBottom!!.setValueIndex(valueIndex)
        mSliderModeBottom!!.setSummary(mSliderModeBottom!!.getEntries().get(valueIndex))
        mHBMModeSwitch = findPreference(KEY_HBM_SWITCH) as TwoStatePreference?
        mHBMModeSwitch!!.setEnabled(HBMModeSwitch.isSupported)
        mHBMModeSwitch!!.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()))
        mHBMModeSwitch!!.setOnPreferenceChangeListener(HBMModeSwitch(getContext()))
        mDCDModeSwitch = findPreference(KEY_DCD_SWITCH) as TwoStatePreference?
        mDCDModeSwitch!!.setEnabled(DCDModeSwitch.isSupported)
        mDCDModeSwitch!!.setChecked(DCDModeSwitch.isCurrentlyEnabled(this.getContext()))
        mDCDModeSwitch!!.setOnPreferenceChangeListener(DCDModeSwitch(getContext()))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return super.onPreferenceTreeClick(preference)
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        if (preference === mSliderModeTop) {
            val value = newValue as String?
            val sliderMode: Int = Integer.valueOf(value)
            setSliderAction(0, sliderMode)
            val valueIndex: Int = mSliderModeTop!!.findIndexOfValue(value)
            mSliderModeTop!!.setSummary(mSliderModeTop!!.getEntries().get(valueIndex))
        } else if (preference === mSliderModeCenter) {
            val value = newValue as String?
            val sliderMode: Int = Integer.valueOf(value)
            setSliderAction(1, sliderMode)
            val valueIndex: Int = mSliderModeCenter!!.findIndexOfValue(value)
            mSliderModeCenter!!.setSummary(mSliderModeCenter!!.getEntries().get(valueIndex))
        } else if (preference === mSliderModeBottom) {
            val value = newValue as String?
            val sliderMode: Int = Integer.valueOf(value)
            setSliderAction(2, sliderMode)
            val valueIndex: Int = mSliderModeBottom!!.findIndexOfValue(value)
            mSliderModeBottom!!.setSummary(mSliderModeBottom!!.getEntries().get(valueIndex))
        }
        return true
    }

    private fun getSliderAction(position: Int): Int {
        var value: String? = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.OMNI_BUTTON_EXTRA_KEY_MAPPING)
        val defaultValue = SLIDER_DEFAULT_VALUE
        if (value == null) {
            value = defaultValue
        } else if (value.indexOf(",") === -1) {
            value = defaultValue
        }
        try {
            val parts: List<String> = value.split(",")
            return Integer.valueOf(parts[position])
        } catch (e: Exception) {
        }
        return 0
    }

    private fun setSliderAction(position: Int, action: Int) {
        var value: String? = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.OMNI_BUTTON_EXTRA_KEY_MAPPING)
        val defaultValue = SLIDER_DEFAULT_VALUE
        if (value == null) {
            value = defaultValue
        } else if (value.indexOf(",") === -1) {
            value = defaultValue
        }
        try {
            val parts: MutableList<String> = value.split(",") as MutableList<String>
            parts[position] = action.toString()
            val newValue: String = TextUtils.join(",", parts)
            Settings.System.putString(getContext().getContentResolver(),
                    Settings.System.OMNI_BUTTON_EXTRA_KEY_MAPPING, newValue)
        } catch (e: Exception) {
        }
    }

    companion object {
        const val KEY_SYSTEM_VIBSTRENGTH = "vib_system_strength"
        const val KEY_CALL_VIBSTRENGTH = "vib_call_strength"
        const val KEY_NOTIF_VIBSTRENGTH = "vib_notif_strength"
        private const val KEY_SLIDER_MODE_TOP = "slider_mode_top"
        private const val KEY_SLIDER_MODE_CENTER = "slider_mode_center"
        private const val KEY_SLIDER_MODE_BOTTOM = "slider_mode_bottom"
        private const val KEY_CATEGORY_GRAPHICS = "graphics"
        const val KEY_SRGB_SWITCH = "srgb"
        const val KEY_HBM_SWITCH = "hbm"
        const val KEY_PROXI_SWITCH = "proxi"
        const val KEY_DCD_SWITCH = "dcd"
        const val KEY_DCI_SWITCH = "dci"
        const val KEY_WIDE_SWITCH = "wide"
        const val KEY_BACKLIGHT_DIMMER = "backlight_dimmer"
        const val KEY_HEADPHONE_GAIN = "headphone_gain"
        const val KEY_EARPIECE_GAIN = "earpiece_gain"
        const val KEY_MIC_GAIN = "mic_gain"
        const val KEY_SPEAKER_GAIN = "speaker_gain"
        const val SLIDER_DEFAULT_VALUE = "2,1,0"
        const val KEY_SETTINGS_PREFIX = "device_setting_"
        private var mHBMModeSwitch: TwoStatePreference? = null
        private var mDCDModeSwitch: TwoStatePreference? = null
    }
}