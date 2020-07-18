/*
* Copyright (C) 2013 The OmniROM Project
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

class Startup : BroadcastReceiver() {
    override fun onReceive(context: Context, bootintent: Intent?) {
        restoreAfterUserSwitch(context)
    }

    companion object {
        private fun restore(file: String?, enabled: Boolean) {
            if (file == null) {
                return
            }
            Utils.writeValue(file, if (enabled) "1" else "0")
        }

        private fun getGestureFile(key: String?): String? {
            return GestureSettings.getGestureFile(key)
        }

        fun restoreAfterUserSwitch(context: Context) {

            // music playback
            val musicPlaybackEnabled = Settings.System.getInt(context.getContentResolver(),
                    "Settings.System." + com.aicp.device.KeyHandler.GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME, 0) === 1
            GestureSettings.setMusicPlaybackGestureEnabled(musicPlaybackEnabled)

            // circle -> camera
            var mapping: String = GestureSettings.DEVICE_GESTURE_MAPPING_1
            var value: String = Settings.System.getString(context.getContentResolver(), mapping)
            if (TextUtils.isEmpty(value)) {
                value = AppSelectListPreference.CAMERA_ENTRY
                Settings.System.putString(context.getContentResolver(), mapping, value)
            }
            var enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_CIRCLE_APP), enabled)

            // down arrow -> flashlight
            mapping = GestureSettings.DEVICE_GESTURE_MAPPING_2
            value = Settings.System.getString(context.getContentResolver(), mapping)
            if (TextUtils.isEmpty(value)) {
                value = AppSelectListPreference.TORCH_ENTRY
                Settings.System.putString(context.getContentResolver(), mapping, value)
            }
            enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_DOWN_ARROW_APP), enabled)

            // down swipe
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_6)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_DOWN_SWIPE_APP), enabled)

            // up swipe
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_7)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_UP_SWIPE_APP), enabled)

            // left swipe
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_8)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_LEFT_SWIPE_APP), enabled)

            // right swipe
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_9)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_RIGHT_SWIPE_APP), enabled)

            // M Gesture
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_3)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_M_GESTURE_APP), enabled)

            // S Gesture
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_4)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_S_GESTURE_APP), enabled)

            // W Gesture
            value = Settings.System.getString(context.getContentResolver(), GestureSettings.DEVICE_GESTURE_MAPPING_5)
            enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)
            restore(getGestureFile(GestureSettings.KEY_W_GESTURE_APP), enabled)
            enabled = Settings.System.getInt(context.getContentResolver(), SRGBModeSwitch.SETTINGS_KEY, 0) !== 0
            restore(SRGBModeSwitch.Companion.file, enabled)
            enabled = Settings.System.getInt(context.getContentResolver(), DCDModeSwitch.SETTINGS_KEY, 0) !== 0
            restore(DCDModeSwitch.Companion.file, enabled)
            enabled = Settings.System.getInt(context.getContentResolver(), DCIModeSwitch.SETTINGS_KEY, 0) !== 0
            restore(DCIModeSwitch.Companion.file, enabled)
            enabled = Settings.System.getInt(context.getContentResolver(), WideModeSwitch.SETTINGS_KEY, 0) !== 0
            restore(WideModeSwitch.Companion.file, enabled)
            enabled = Settings.System.getInt(context.getContentResolver(), HBMModeSwitch.SETTINGS_KEY, 0) !== 0
            restore(HBMModeSwitch.Companion.file, enabled)
            VibratorSystemStrengthPreference.restore(context)
            VibratorCallStrengthPreference.restore(context)
            VibratorNotifStrengthPreference.restore(context)
            BacklightDimmerPreference.restore(context)
            HeadphoneGainPreference.restore(context)
            EarpieceGainPreference.restore(context)
            MicGainPreference.restore(context)
            SpeakerGainPreference.restore(context)
        }
    }
}