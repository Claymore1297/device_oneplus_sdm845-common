/*
* Copyright (C) 2018 The OmniROM Project
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

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager

@TargetApi(24)
class HBMModeTileService : TileService() {
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTileAdded() {
        super.onTileAdded()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled: Boolean = HBMModeSwitch.isCurrentlyEnabled()
        Utils.writeValue(HBMModeSwitch.Companion.file, if (enabled) "0" else "1")
        sharedPrefs.edit().putBoolean(DeviceSettings.KEY_HBM_SWITCH, !enabled).apply()
    }
}
