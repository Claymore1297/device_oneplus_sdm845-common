/*
* Copyright (C) 2017 The OmniROM Project
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
import com.aicp.device.Utils.fileWritable
import com.aicp.device.Utils.getFileValueAsBoolean
import com.aicp.device.Utils.writeValue

class WideModeSwitch(context: Context) : OnPreferenceChangeListener {
    private val mContext: Context
    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        val enabled = newValue as Boolean
        Settings.System.putInt(mContext.getContentResolver(), SETTINGS_KEY, if (enabled) 1 else 0)
        writeValue(file, if (enabled) "1" else "0")
        return true
    }

    companion object {
        private const val FILE = "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/native_display_wide_color_mode"
        const val SETTINGS_KEY = DeviceSettings.KEY_SETTINGS_PREFIX + DeviceSettings.KEY_WIDE_SWITCH
        @JvmStatic
        val file: String?
            get() = if (fileWritable(FILE)) {
                FILE
            } else null

        val isSupported: Boolean
            get() = fileWritable(file)

        @JvmStatic
        fun isCurrentlyEnabled(context: Context?): Boolean {
            return getFileValueAsBoolean(file, false)
        }
    }

    init {
        mContext = context
    }
}