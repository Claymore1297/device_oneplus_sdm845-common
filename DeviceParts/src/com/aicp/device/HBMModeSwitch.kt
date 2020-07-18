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

import android.content.Context
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener

class HBMModeSwitch(context: Context) : OnPreferenceChangeListener {
    private val mContext: Context = context
    private val mHBMOffState: String = context.resources.getString(R.string.hbmOFF)
    private val mHBMOnState: String = context.resources.getString(R.string.hbmON)
    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        val enabled = newValue as Boolean
        Settings.System.putInt(mContext.contentResolver, SETTINGS_KEY, if (enabled) 1 else 0)
        Utils.writeValue(file, if (enabled) mHBMOnState else mHBMOffState)
        return true
    }

    companion object {
        private const val FILE = "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/hbm"
        const val SETTINGS_KEY = DeviceSettings.KEY_SETTINGS_PREFIX + DeviceSettings.KEY_HBM_SWITCH
        @JvmStatic
        val file: String?
            get() = if (Utils.fileWritable(FILE)) {
                FILE
            } else null

        val isSupported: Boolean
            get() = Utils.fileWritable(file)

        @JvmStatic
        fun isCurrentlyEnabled(context: Context?): Boolean {
            return Utils.getFileValueAsBoolean(file, false)
        }
    }
}
