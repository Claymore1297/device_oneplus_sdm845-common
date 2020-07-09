/*
* Copyright (C) 2016 The OmniROM Project
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

import android.app.ActivityManagerNative
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.IAudioService
import android.media.session.MediaSessionLegacyHelper
import android.net.Uri
import android.os.Bundle
import android.os.FileObserver
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.RemoteException
import android.os.ServiceManager
import android.os.SystemClock
import android.os.SystemProperties
import android.os.UEventObserver
import android.os.UserHandle
import android.os.VibrationEffect
import android.provider.Settings
import android.provider.Settings.Global
import android.provider.Settings.Global.ZEN_MODE_ALARMS
import android.provider.Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS
import android.provider.Settings.Global.ZEN_MODE_NO_INTERRUPTIONS
import android.provider.Settings.Global.ZEN_MODE_OFF
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.WindowManagerGlobal
import com.android.internal.statusbar.IStatusBarService
import com.android.internal.util.ArrayUtils
import com.android.internal.util.aicp.AicpUtils
import com.android.internal.util.aicp.AicpVibe
import com.android.internal.util.aicp.CustomKeyHandler
import com.android.internal.util.aicp.PackageUtils

class KeyHandler(context: Context) : CustomKeyHandler {
    protected val mContext: Context
    private val mPowerManager: PowerManager
    private val mEventHandler: EventHandler
    private val mGestureWakeLock: WakeLock
    private val mNoMan: NotificationManager
    private val mAudioManager: AudioManager
    private val mProxyIsNear = false
    private var mDispOn: Boolean
    private var mRestoreUser = false
    private var mUseSliderTorch = false
    private var mTorchState = false
    private val mSystemStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mDispOn = true
                onDisplayOn()
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mDispOn = false
                onDisplayOff()
            } else if (intent.getAction().equals(Intent.ACTION_USER_SWITCHED)) {
                val userId: Int = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, UserHandle.USER_NULL)
                if (userId == UserHandle.USER_SYSTEM && mRestoreUser) {
                    if (DEBUG) Log.i(TAG, "ACTION_USER_SWITCHED to system")
                    Startup.restoreAfterUserSwitch(context)
                } else {
                    mRestoreUser = true
                }
            }
        }
    }

    private fun hasSetupCompleted(): Boolean {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0) !== 0
    }

    private inner class EventHandler : Handler() {
        override fun handleMessage(msg: Message?) {}
    }

    override fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.getAction() !== KeyEvent.ACTION_UP) {
            return false
        }
        return if (!hasSetupCompleted()) {
            false
        } else false
    }

    override fun canHandleKeyEvent(event: KeyEvent): Boolean {
        return ArrayUtils.contains(sSupportedGestures, event.getScanCode())
    }

    override fun isDisabledKeyEvent(event: KeyEvent?): Boolean {
        return false
    }

    override fun isCameraLaunchEvent(event: KeyEvent): Boolean {
        if (event.getAction() !== KeyEvent.ACTION_UP) {
            return false
        }
        val value = getGestureValueForScanCode(event.getScanCode())
        return !TextUtils.isEmpty(value) && value == AppSelectListPreference.CAMERA_ENTRY
    }

    override fun isWakeEvent(event: KeyEvent): Boolean {
        if (event.getAction() !== KeyEvent.ACTION_UP) {
            return false
        }
        val value = getGestureValueForScanCode(event.getScanCode())
        if (!TextUtils.isEmpty(value) && value == AppSelectListPreference.WAKE_ENTRY) {
            if (DEBUG) Log.i(TAG, "isWakeEvent " + event.getScanCode().toString() + value)
            return true
        }
        return event.getScanCode() === KEY_DOUBLE_TAP
    }

    override fun isActivityLaunchEvent(event: KeyEvent): Intent? {
        if (event.getAction() !== KeyEvent.ACTION_UP) {
            return null
        }
        val value = getGestureValueForScanCode(event.getScanCode())
        if (!TextUtils.isEmpty(value) && value != AppSelectListPreference.DISABLED_ENTRY) {
            if (DEBUG) Log.i(TAG, "isActivityLaunchEvent " + event.getScanCode().toString() + value)
            if (!launchSpecialActions(value)) {
                AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
                return createIntent(value)
            }
        }
        return null
    }

    private val audioService: IAudioService?
        private get() {
            val audioService: IAudioService = IAudioService.Stub
                    .asInterface(ServiceManager.checkService(Context.AUDIO_SERVICE))
            if (audioService == null) {
                Log.w(TAG, "Unable to find IAudioService interface.")
            }
            return audioService
        }

    val isMusicActive: Boolean
        get() = mAudioManager.isMusicActive()

    private fun dispatchMediaKeyWithWakeLockToAudioService(keycode: Int) {
        if (ActivityManagerNative.isSystemReady()) {
            val audioService: IAudioService? = audioService
            if (audioService != null) {
                var event = KeyEvent(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                        keycode, 0)
                dispatchMediaKeyEventUnderWakelock(event)
                event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP)
                dispatchMediaKeyEventUnderWakelock(event)
            }
        }
    }

    private fun dispatchMediaKeyEventUnderWakelock(event: KeyEvent) {
        if (ActivityManagerNative.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(mContext).sendMediaButtonEvent(event, true)
        }
    }

    private fun onDisplayOn() {
        if (DEBUG) Log.i(TAG, "Display on")
    }

    private fun onDisplayOff() {
        if (DEBUG) Log.i(TAG, "Display off")
    }

    private fun getSliderAction(position: Int): Int {
        var value: String? = Settings.System.getStringForUser(mContext.getContentResolver(),
                Settings.System.OMNI_BUTTON_EXTRA_KEY_MAPPING,
                UserHandle.USER_CURRENT)
        val defaultValue = DeviceSettings.SLIDER_DEFAULT_VALUE
        if (value == null) {
            value = defaultValue
        } else if (value.indexOf(",") == -1) {
            value = defaultValue
        }
        try {
            val parts = value.split(",".toRegex()).toTypedArray()
            return Integer.valueOf(parts[position])
        } catch (e: Exception) {
        }
        return 0
    }

    private fun doHandleSliderAction(position: Int) {
        val action = getSliderAction(position)
        var positionValue = 0
        when (action) {
            0 -> {
                mNoMan.setZenMode(ZEN_MODE_OFF, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                mTorchState = false
                positionValue = MODE_RING
            }
            1 -> {
                mNoMan.setZenMode(ZEN_MODE_OFF, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE)
                mTorchState = false
                positionValue = MODE_VIBRATE
            }
            2 -> {
                mNoMan.setZenMode(ZEN_MODE_OFF, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT)
                mTorchState = false
                positionValue = MODE_SILENT
            }
            3 -> {
                mNoMan.setZenMode(ZEN_MODE_IMPORTANT_INTERRUPTIONS, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                mTorchState = false
                positionValue = MODE_PRIORITY_ONLY
            }
            4 -> {
                mNoMan.setZenMode(ZEN_MODE_ALARMS, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                mTorchState = false
                positionValue = MODE_ALARMS_ONLY
            }
            5 -> {
                mNoMan.setZenMode(ZEN_MODE_NO_INTERRUPTIONS, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                mTorchState = false
                positionValue = MODE_TOTAL_SILENCE
            }
            6 -> {
                mNoMan.setZenMode(ZEN_MODE_OFF, null, TAG)
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                positionValue = MODE_FLASHLIGHT
                mUseSliderTorch = true
                mTorchState = true
            }
        }
        if (positionValue != 0) {
            sendUpdateBroadcast(position, positionValue)
        }
        if (!mProxyIsNear && mUseSliderTorch && action < 4) {
            launchSpecialActions(AppSelectListPreference.TORCH_ENTRY)
            mUseSliderTorch = false
        } else if (!mProxyIsNear && mUseSliderTorch) {
            launchSpecialActions(AppSelectListPreference.TORCH_ENTRY)
        }
    }

    private fun sendUpdateBroadcast(position: Int, position_value: Int) {
        val extras = Bundle()
        val intent = Intent(ACTION_UPDATE_SLIDER_POSITION)
        extras.putInt(EXTRA_SLIDER_POSITION, position)
        extras.putInt(EXTRA_SLIDER_POSITION_VALUE, position_value)
        intent.putExtras(extras)
        mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT)
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        Log.d(TAG, "slider change to positon " + position
                + " with value " + position_value)
    }

    private fun createIntent(value: String?): Intent {
        val componentName: ComponentName = ComponentName.unflattenFromString(value)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        intent.setComponent(componentName)
        return intent
    }

    private fun launchSpecialActions(value: String?): Boolean {
        val musicPlaybackEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                "Settings.System.$GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME", 0, UserHandle.USER_CURRENT) === 1
        /* handle music playback gesture if enabled */if (musicPlaybackEnabled) {
            when (value) {
                AppSelectListPreference.MUSIC_PLAY_ENTRY -> {
                    mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION)
                    AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
                    dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    return true
                }
                AppSelectListPreference.MUSIC_NEXT_ENTRY -> {
                    if (isMusicActive) {
                        mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION)
                        AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
                        dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_NEXT)
                    }
                    return true
                }
                AppSelectListPreference.MUSIC_PREV_ENTRY -> {
                    if (isMusicActive) {
                        mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION)
                        AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
                        dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    }
                    return true
                }
            }
        }
        if (value == AppSelectListPreference.TORCH_ENTRY) {
            mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION)
            val service: IStatusBarService = statusBarService
            if (service != null) {
                try {
                    service.toggleCameraFlash()
                    AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
                    return true
                } catch (e: RemoteException) {
                    // do nothing.
                }
            }
        } else if (value == AppSelectListPreference.AMBIENT_DISPLAY_ENTRY) {
            AicpVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, mContext, GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, GESTURE_HAPTIC_DURATION)
            launchDozePulse()
            return true
        }
        return false
    }

    private fun getGestureValueForScanCode(scanCode: Int): String? {
        /* for the music playback gestures, just return the expected values */
        when (scanCode) {
            GESTURE_II_SCANCODE -> return AppSelectListPreference.MUSIC_PLAY_ENTRY
            GESTURE_CIRCLE_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_1, UserHandle.USER_CURRENT)
            GESTURE_V_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_2, UserHandle.USER_CURRENT)
            GESTURE_M_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_3, UserHandle.USER_CURRENT)
            GESTURE_S_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_4, UserHandle.USER_CURRENT)
            GESTURE_W_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_5, UserHandle.USER_CURRENT)
            GESTURE_LEFT_V_SCANCODE -> return AppSelectListPreference.MUSIC_PREV_ENTRY
            GESTURE_RIGHT_V_SCANCODE -> return AppSelectListPreference.MUSIC_NEXT_ENTRY
            GESTURE_DOWN_SWIPE_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_6, UserHandle.USER_CURRENT)
            GESTURE_UP_SWIPE_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_7, UserHandle.USER_CURRENT)
            GESTURE_LEFT_SWIPE_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_8, UserHandle.USER_CURRENT)
            GESTURE_RIGHT_SWIPE_SCANCODE -> return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_9, UserHandle.USER_CURRENT)
        }
        return null
    }

    private fun launchDozePulse() {
        if (DEBUG) Log.i(TAG, "Doze pulse")
        mContext.sendBroadcastAsUser(Intent(DOZE_INTENT),
                UserHandle(UserHandle.USER_CURRENT))
    }

    val statusBarService: IStatusBarService
        get() = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"))

    override fun getCustomProxiIsNear(event: SensorEvent): Boolean {
        return event.values.get(0).toInt() === 1
    }

    private inner class ClientPackageNameObserver(file: String?) : FileObserver(CLIENT_PACKAGE_PATH, MODIFY) {
        override fun onEvent(event: Int, file: String?) {
            val pkgName = Utils.getFileValue(CLIENT_PACKAGE_PATH, "0")
        }
    }

    companion object {
        private const val TAG = "KeyHandler"
        private const val DEBUG = false
        private const val DEBUG_SENSOR = false
        protected const val GESTURE_REQUEST = 1
        private const val GESTURE_WAKELOCK_DURATION = 2000L
        const val GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME = "OFF_GESTURE_HAPTIC_ENABLE"
        const val GESTURE_MUSIC_PLAYBACK_SETTINGS_VARIABLE_NAME = "MUSIC_PLAYBACK_GESTURE_ENABLE"
        private const val GESTURE_HAPTIC_DURATION = 50
        private const val DT2W_CONTROL_PATH = "/proc/touchpanel/double_tap_enable"
        const val ACTION_UPDATE_SLIDER_POSITION = "com.aicp.device.UPDATE_SLIDER_POSITION"
        const val EXTRA_SLIDER_POSITION = "position"
        const val EXTRA_SLIDER_POSITION_VALUE = "position_value"
        private const val GESTURE_W_SCANCODE = 246
        private const val GESTURE_M_SCANCODE = 247
        private const val GESTURE_S_SCANCODE = 248
        private const val GESTURE_CIRCLE_SCANCODE = 250
        private const val GESTURE_V_SCANCODE = 252
        private const val GESTURE_II_SCANCODE = 251
        private const val GESTURE_LEFT_V_SCANCODE = 253
        private const val GESTURE_RIGHT_V_SCANCODE = 254
        private const val GESTURE_RIGHT_SWIPE_SCANCODE = 63
        private const val GESTURE_LEFT_SWIPE_SCANCODE = 64
        private const val GESTURE_DOWN_SWIPE_SCANCODE = 65
        private const val GESTURE_UP_SWIPE_SCANCODE = 66
        private const val KEY_DOUBLE_TAP = 143
        private const val KEY_HOME = 102
        private const val KEY_BACK = 158
        private const val KEY_RECENTS = 580
        private const val KEY_SLIDER_TOP = 601
        private const val KEY_SLIDER_CENTER = 602
        private const val KEY_SLIDER_BOTTOM = 603
        private const val MIN_PULSE_INTERVAL_MS = 2500
        private const val DOZE_INTENT = "com.android.systemui.doze.pulse"
        private const val HANDWAVE_MAX_DELTA_MS = 1000
        private const val POCKET_MIN_DELTA_MS = 5000
        private const val FP_GESTURE_LONG_PRESS = 305
        const val CLIENT_PACKAGE_NAME = "com.oneplus.camera"
        const val CLIENT_PACKAGE_PATH = "/data/misc/omni/client_package_name"

        // TriStateUI Modes
        const val MODE_TOTAL_SILENCE = 600
        const val MODE_ALARMS_ONLY = 601
        const val MODE_PRIORITY_ONLY = 602
        const val MODE_NONE = 603
        const val MODE_VIBRATE = 604
        const val MODE_RING = 605

        // AICP additions: arbitrary value which hopefully doesn't conflict with upstream anytime soon
        const val MODE_SILENT = 620
        const val MODE_FLASHLIGHT = 621
        private val sSupportedGestures = intArrayOf(
                GESTURE_II_SCANCODE,
                GESTURE_CIRCLE_SCANCODE,
                GESTURE_V_SCANCODE,
                GESTURE_M_SCANCODE,
                GESTURE_S_SCANCODE,
                GESTURE_W_SCANCODE,
                GESTURE_LEFT_V_SCANCODE,
                GESTURE_RIGHT_V_SCANCODE,
                GESTURE_DOWN_SWIPE_SCANCODE,
                GESTURE_UP_SWIPE_SCANCODE,
                GESTURE_LEFT_SWIPE_SCANCODE,
                GESTURE_RIGHT_SWIPE_SCANCODE,
                KEY_DOUBLE_TAP,
                KEY_SLIDER_TOP,
                KEY_SLIDER_CENTER,
                KEY_SLIDER_BOTTOM
        )
        private val sProxiCheckedGestures = intArrayOf(
                GESTURE_II_SCANCODE,
                GESTURE_CIRCLE_SCANCODE,
                GESTURE_V_SCANCODE,
                GESTURE_M_SCANCODE,
                GESTURE_S_SCANCODE,
                GESTURE_W_SCANCODE,
                GESTURE_LEFT_V_SCANCODE,
                GESTURE_RIGHT_V_SCANCODE,
                GESTURE_DOWN_SWIPE_SCANCODE,
                GESTURE_UP_SWIPE_SCANCODE,
                GESTURE_LEFT_SWIPE_SCANCODE,
                GESTURE_RIGHT_SWIPE_SCANCODE,
                KEY_DOUBLE_TAP
        )
        private const val mButtonDisabled = false
        protected fun getSensor(sm: SensorManager, type: String): Sensor? {
            for (sensor in sm.getSensorList(Sensor.TYPE_ALL)) {
                if (type == sensor.getStringType()) {
                    return sensor
                }
            }
            return null
        }
    }

    init {
        mContext = context
        mDispOn = true
        mEventHandler = EventHandler()
        mPowerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        mGestureWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GestureWakeLock")
        mNoMan = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val systemStateFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        systemStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        systemStateFilter.addAction(Intent.ACTION_USER_SWITCHED)
        mContext.registerReceiver(mSystemStateReceiver, systemStateFilter)
        object : UEventObserver() {
            override fun onUEvent(event: UEventObserver.UEvent) {
                try {
                    val state: String = event.get("STATE")
                    val ringing = state.contains("USB=0")
                    val silent = state.contains("(null)=0")
                    val vibrate = state.contains("USB_HOST=0")
                    Log.v(TAG, "Got ringing = $ringing, silent = $silent, vibrate = $vibrate")
                    if (ringing && !silent && !vibrate) doHandleSliderAction(2)
                    if (silent && !ringing && !vibrate) doHandleSliderAction(0)
                    if (vibrate && !silent && !ringing) doHandleSliderAction(1)
                } catch (e: Exception) {
                    Log.d(TAG, "Failed parsing uevent", e)
                }
            }
        }.startObserving("DEVPATH=/devices/platform/soc/soc:tri_state_key")
    }
}
