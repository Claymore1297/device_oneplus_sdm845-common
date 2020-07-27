/*
* Copyright (C) 2017 The OmniROM Project
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

import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.Secure.SYSTEM_NAVIGATION_KEYS_ENABLED
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.TwoStatePreference
import java.util.*


class GestureSettings : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    private var mMusicPlaybackGestureSwitch: TwoStatePreference? = null
    private var mOffscreenGestureFeedbackSwitch: TwoStatePreference? = null
    private var mCircleApp: ListPreference? = null
    private var mDownArrowApp: ListPreference? = null
    private var mUpSwipeApp: ListPreference? = null
    private var mDownSwipeApp: ListPreference? = null
    private var mLeftSwipeApp: ListPreference? = null
    private var mRightSwipeApp: ListPreference? = null
    private var mMGestureApp: ListPreference? = null
    private var mSGestureApp: ListPreference? = null
    private var mWGestureApp: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.gesture_settings, rootKey)
        mOffscreenGestureFeedbackSwitch = findPreference(KEY_OFF_SCREEN_GESTURE_FEEDBACK_SWITCH) as TwoStatePreference?
        mOffscreenGestureFeedbackSwitch!!.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                "Settings.System." + KeyHandler.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, 1) !== 0)
        mMusicPlaybackGestureSwitch = findPreference(KEY_MUSIC_START) as TwoStatePreference?
        mMusicPlaybackGestureSwitch!!.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                "Settings.System." + KeyHandler.GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME, 1) !== 0)
        val musicPlaybackEnabled = Settings.System.getIntForUser(getContext().getContentResolver(),
                "Settings.System." + KeyHandler.GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME, 0, UserHandle.USER_CURRENT) === 1
        setMusicPlaybackGestureEnabled(musicPlaybackEnabled)

        mCircleApp = findPreference(KEY_CIRCLE_APP) as ListPreference?
        mCircleApp!!.setOnPreferenceChangeListener(this)
        var mapping: String = GestureSettings.DEVICE_GESTURE_MAPPING_1
        var value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        var valueIndex: Int = mCircleApp!!.findIndexOfValue(value)
        mCircleApp!!.setValueIndex(valueIndex)
        mCircleApp!!.setSummary(mCircleApp!!.getEntries().get(valueIndex))

        mDownArrowApp = findPreference(KEY_DOWN_ARROW_APP) as ListPreference?
        mDownArrowApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_2
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mDownArrowApp!!.findIndexOfValue(value)
        mDownArrowApp!!.setValueIndex(valueIndex)
        mDownArrowApp!!.setSummary(mDownArrowApp!!.getEntries().get(valueIndex))

        mMGestureApp = findPreference(KEY_M_GESTURE_APP) as ListPreference?
        mMGestureApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_3
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mMGestureApp!!.findIndexOfValue(value)
        mMGestureApp!!.setValueIndex(valueIndex)
        mMGestureApp!!.setSummary(mMGestureApp!!.getEntries().get(valueIndex))

        mSGestureApp = findPreference(KEY_S_GESTURE_APP) as ListPreference?
        mSGestureApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_4
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mSGestureApp!!.findIndexOfValue(value)
        mSGestureApp!!.setValueIndex(valueIndex)
        mSGestureApp!!.setSummary(mSGestureApp!!.getEntries().get(valueIndex))

        mWGestureApp = findPreference(KEY_W_GESTURE_APP) as ListPreference?
        mWGestureApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_5
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mWGestureApp!!.findIndexOfValue(value)
        mWGestureApp!!.setValueIndex(valueIndex)
        mWGestureApp!!.setSummary(mWGestureApp!!.getEntries().get(valueIndex))

        mDownSwipeApp = findPreference(KEY_DOWN_SWIPE_APP) as ListPreference?
        mDownSwipeApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_6
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mDownSwipeApp!!.findIndexOfValue(value)
        mDownSwipeApp!!.setValueIndex(valueIndex)
        mDownSwipeApp!!.setSummary(mDownSwipeApp!!.getEntries().get(valueIndex))

        mUpSwipeApp = findPreference(KEY_UP_SWIPE_APP) as ListPreference?
        mUpSwipeApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_7
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mUpSwipeApp!!.findIndexOfValue(value)
        mUpSwipeApp!!.setValueIndex(valueIndex)
        mUpSwipeApp!!.setSummary(mUpSwipeApp!!.getEntries().get(valueIndex))

        mLeftSwipeApp = findPreference(KEY_LEFT_SWIPE_APP) as ListPreference?
        mLeftSwipeApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_8
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mLeftSwipeApp!!.findIndexOfValue(value)
        mLeftSwipeApp!!.setValueIndex(valueIndex)
        mLeftSwipeApp!!.setSummary(mLeftSwipeApp!!.getEntries().get(valueIndex))

        mRightSwipeApp = findPreference(KEY_RIGHT_SWIPE_APP) as ListPreference?
        mRightSwipeApp!!.setOnPreferenceChangeListener(this)
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_9
        value = "disabled"
        if (Settings.System.getString(context.getContentResolver(), mapping) !=null) {
            value = Settings.System.getString(context.getContentResolver(), mapping)
        }
        valueIndex = mRightSwipeApp!!.findIndexOfValue(value)
        mRightSwipeApp!!.setValueIndex(valueIndex)
        mRightSwipeApp!!.setSummary(mRightSwipeApp!!.getEntries().get(valueIndex))
    }

    private fun areSystemNavigationKeysEnabled(): Boolean {
        return Settings.Secure.getInt(getContext().getContentResolver(),
                Settings.Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, 0) === 1
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference === mOffscreenGestureFeedbackSwitch) {
            Settings.System.putInt(getContext().getContentResolver(),
                    "Settings.System." + KeyHandler.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, if (mOffscreenGestureFeedbackSwitch!!.isChecked()) 1 else 0)
            return true
        }
        if (preference === mMusicPlaybackGestureSwitch) {
            Settings.System.putInt(getContext().getContentResolver(),
                    "Settings.System." + KeyHandler.GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME, if (mMusicPlaybackGestureSwitch!!.isChecked()) 1 else 0)
            setMusicPlaybackGestureEnabled(mMusicPlaybackGestureSwitch!!.isChecked())
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        if (preference === mCircleApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_CIRCLE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_1, value)
            var valueIndex: Int = mCircleApp!!.findIndexOfValue(value)
            mCircleApp!!.setValueIndex(valueIndex)
            mCircleApp!!.setSummary(mCircleApp!!.getEntries().get(valueIndex))
        } else if (preference === mDownArrowApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_DOWN_ARROW_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_2, value)
            var valueIndex: Int = mDownArrowApp!!.findIndexOfValue(value)
            mDownArrowApp!!.setValueIndex(valueIndex)
            mDownArrowApp!!.setSummary(mDownArrowApp!!.getEntries().get(valueIndex))
        } else if (preference === mMGestureApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_M_GESTURE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_3, value)
            var valueIndex: Int = mMGestureApp!!.findIndexOfValue(value)
            mMGestureApp!!.setValueIndex(valueIndex)
            mMGestureApp!!.setSummary(mMGestureApp!!.getEntries().get(valueIndex))
        } else if (preference === mSGestureApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_S_GESTURE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_4, value)
            var valueIndex: Int = mSGestureApp!!.findIndexOfValue(value)
            mSGestureApp!!.setValueIndex(valueIndex)
            mSGestureApp!!.setSummary(mSGestureApp!!.getEntries().get(valueIndex))
        } else if (preference === mWGestureApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_W_GESTURE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_5, value)
            var valueIndex: Int = mWGestureApp!!.findIndexOfValue(value)
            mWGestureApp!!.setValueIndex(valueIndex)
            mWGestureApp!!.setSummary(mWGestureApp!!.getEntries().get(valueIndex))
        } else if (preference === mDownSwipeApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_DOWN_SWIPE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_6, value)
            var valueIndex: Int = mDownSwipeApp!!.findIndexOfValue(value)
            mDownSwipeApp!!.setValueIndex(valueIndex)
            mDownSwipeApp!!.setSummary(mDownSwipeApp!!.getEntries().get(valueIndex))
        } else if (preference === mUpSwipeApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_UP_SWIPE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_7, value)
            var valueIndex: Int = mUpSwipeApp!!.findIndexOfValue(value)
            mUpSwipeApp!!.setValueIndex(valueIndex)
            mUpSwipeApp!!.setSummary(mUpSwipeApp!!.getEntries().get(valueIndex))
        } else if (preference === mLeftSwipeApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_LEFT_SWIPE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_8, value)
            var valueIndex: Int = mLeftSwipeApp!!.findIndexOfValue(value)
            mLeftSwipeApp!!.setValueIndex(valueIndex)
            mLeftSwipeApp!!.setSummary(mLeftSwipeApp!!.getEntries().get(valueIndex))
        } else if (preference === mRightSwipeApp) {
            val value = newValue as String
            val gestureDisabled = value.equals(DISABLED_ENTRY)
            setGestureEnabled(KEY_RIGHT_SWIPE_APP, !gestureDisabled)
            Settings.System.putString(getContext().getContentResolver(), DEVICE_GESTURE_MAPPING_9, value)
            var valueIndex: Int = mRightSwipeApp!!.findIndexOfValue(value)
            mRightSwipeApp!!.setValueIndex(valueIndex)
            mRightSwipeApp!!.setSummary(mRightSwipeApp!!.getEntries().get(valueIndex))
        }
        return true
    }

    companion object {
        const val TAG = "GestureSettings"
        const val KEY_PROXI_SWITCH = "proxi"
        const val KEY_OFF_SCREEN_GESTURE_FEEDBACK_SWITCH = "off_screen_gesture_feedback"
        const val KEY_MUSIC_START = "music_playback_gesture"
        const val KEY_CIRCLE_APP = "circle_gesture_app"
        const val KEY_DOWN_ARROW_APP = "down_arrow_gesture_app"
        const val KEY_MUSIC_TRACK_PREV = "left_arrow_gesture_app"
        const val KEY_MUSIC_TRACK_NEXT = "right_arrow_gesture_app"
        const val KEY_M_GESTURE_APP = "gesture_m_app"
        const val KEY_S_GESTURE_APP = "gesture_s_app"
        const val KEY_W_GESTURE_APP = "gesture_w_app"
        const val KEY_DOWN_SWIPE_APP = "down_swipe_gesture_app"
        const val KEY_UP_SWIPE_APP = "up_swipe_gesture_app"
        const val KEY_LEFT_SWIPE_APP = "left_swipe_gesture_app"
        const val KEY_RIGHT_SWIPE_APP = "right_swipe_gesture_app"
        const val KEY_FP_GESTURE_CATEGORY = "key_fp_gesture_category"
        const val KEY_FP_GESTURE_DEFAULT_CATEGORY = "gesture_settings"
        const val DEVICE_GESTURE_MAPPING_1 = "device_gesture_mapping_1_0"
        const val DEVICE_GESTURE_MAPPING_2 = "device_gesture_mapping_2_0"
        const val DEVICE_GESTURE_MAPPING_3 = "device_gesture_mapping_3_0"
        const val DEVICE_GESTURE_MAPPING_4 = "device_gesture_mapping_4_0"
        const val DEVICE_GESTURE_MAPPING_5 = "device_gesture_mapping_5_0"
        const val DEVICE_GESTURE_MAPPING_6 = "device_gesture_mapping_6_0"
        const val DEVICE_GESTURE_MAPPING_7 = "device_gesture_mapping_7_0"
        const val DEVICE_GESTURE_MAPPING_8 = "device_gesture_mapping_8_0"
        const val DEVICE_GESTURE_MAPPING_9 = "device_gesture_mapping_9_0"
        const val TORCH_ENTRY = "torch"
        const val DISABLED_ENTRY = "disabled"
        const val CAMERA_ENTRY = "camera"
        const val MUSIC_PLAY_ENTRY = "music_play"
        const val MUSIC_PREV_ENTRY = "music_prev"
        const val MUSIC_NEXT_ENTRY = "music_next"
        const val WAKE_ENTRY = "wake"
        const val AMBIENT_DISPLAY_ENTRY = "ambient_display"
        fun getGestureFile(key: String?): String? {
            when (key) {
                KEY_CIRCLE_APP -> return "/proc/touchpanel/letter_o_enable"
                KEY_MUSIC_START -> return "/proc/touchpanel/double_swipe_enable"
                KEY_DOWN_ARROW_APP -> return "/proc/touchpanel/down_arrow_enable"
                KEY_MUSIC_TRACK_PREV -> return "/proc/touchpanel/left_arrow_enable"
                KEY_MUSIC_TRACK_NEXT -> return "/proc/touchpanel/right_arrow_enable"
                KEY_DOWN_SWIPE_APP -> return "/proc/touchpanel/down_swipe_enable"
                KEY_UP_SWIPE_APP -> return "/proc/touchpanel/up_swipe_enable"
                KEY_LEFT_SWIPE_APP -> return "/proc/touchpanel/left_swipe_enable"
                KEY_RIGHT_SWIPE_APP -> return "/proc/touchpanel/right_swipe_enable"
                KEY_S_GESTURE_APP -> return "/proc/touchpanel/letter_s_enable"
                KEY_W_GESTURE_APP -> return "/proc/touchpanel/letter_w_enable"
                KEY_M_GESTURE_APP -> return "/proc/touchpanel/letter_m_enable"
            }
            return null
        }

        fun setMusicPlaybackGestureEnabled(enabled: Boolean) {
            val musicPlaybackSupported = isGestureSupported(KEY_MUSIC_START)
            val musicNextTrackSupported = isGestureSupported(KEY_MUSIC_TRACK_NEXT)
            val musicPrevTrackSupported = isGestureSupported(KEY_MUSIC_TRACK_PREV)
            if (musicPlaybackSupported && musicNextTrackSupported && musicPrevTrackSupported) {
                if (enabled) {
                    setGestureEnabled(KEY_MUSIC_START, musicPlaybackSupported)
                    setGestureEnabled(KEY_MUSIC_TRACK_NEXT, musicNextTrackSupported)
                    setGestureEnabled(KEY_MUSIC_TRACK_PREV, musicPrevTrackSupported)
                } else {
                    setGestureEnabled(KEY_MUSIC_START, false)
                    setGestureEnabled(KEY_MUSIC_TRACK_NEXT, false)
                    setGestureEnabled(KEY_MUSIC_TRACK_PREV, false)
                }
            } else {
                Log.e(TAG, "music playback gesture files are not writeable!")
            }
        }

        private fun isGestureSupported(key: String): Boolean {
            return Utils.fileWritable(getGestureFile(key))
        }

        private fun setGestureEnabled(key: String, enabled: Boolean) {
            Utils.writeValue(getGestureFile(key), if (enabled) "1" else "0")
        }
    }
}