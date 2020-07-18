/*
* Copyright (C) 2018 The OmniROM Project
* Copyright (C) 2020 The Android Ice Cold Project
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

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.preference.PreferenceFragment

class PanelSettings : PreferenceFragment(), RadioGroup.OnCheckedChangeListener {
    private var mRadioGroup: RadioGroup? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRadioGroup = view.findViewById(R.id.radio_group) as RadioGroup
        var checkedButtonId: Int = R.id.off_mode
        if (WideModeSwitch.isCurrentlyEnabled(getContext())) {
            checkedButtonId = R.id.wide_mode
        } else if (DCIModeSwitch.isCurrentlyEnabled(getContext())) {
            checkedButtonId = R.id.dci_mode
        } else if (SRGBModeSwitch.isCurrentlyEnabled(getContext())) {
            checkedButtonId = R.id.srgb_mode
        }
        mRadioGroup!!.check(checkedButtonId)
        mRadioGroup!!.setOnCheckedChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.panel_modes, container, false)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        Utils.writeValue(DCIModeSwitch.Companion.file, "0")
        Settings.System.putInt(getContext().getContentResolver(), DCIModeSwitch.SETTINGS_KEY, 0)
        Utils.writeValue(WideModeSwitch.Companion.file, "0")
        Settings.System.putInt(getContext().getContentResolver(), WideModeSwitch.SETTINGS_KEY, 0)
        Utils.writeValue(SRGBModeSwitch.Companion.file, "0")
        Settings.System.putInt(getContext().getContentResolver(), SRGBModeSwitch.SETTINGS_KEY, 0)
        when (checkedId) {
            R.id.srgb_mode -> {
                Utils.writeValue(SRGBModeSwitch.Companion.file, "1")
                Settings.System.putInt(getContext().getContentResolver(), SRGBModeSwitch.SETTINGS_KEY, 1)
            }
            R.id.dci_mode -> {
                Utils.writeValue(DCIModeSwitch.Companion.file, "1")
                Settings.System.putInt(getContext().getContentResolver(), DCIModeSwitch.SETTINGS_KEY, 1)
            }
            R.id.wide_mode -> {
                Utils.writeValue(WideModeSwitch.Companion.file, "1")
                Settings.System.putInt(getContext().getContentResolver(), WideModeSwitch.SETTINGS_KEY, 1)
            }
            R.id.off_mode -> {
            }
        }
    }
}