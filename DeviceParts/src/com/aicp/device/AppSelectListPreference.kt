/*
* Copyright (C) 2017 The OmniROM Project
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

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.preference.PreferenceDialogFragment
import androidx.preference.PreferenceViewHolder
import com.android.settingslib.CustomDialogPreference
import java.util.LinkedList

class AppSelectListPreference : CustomDialogPreference {
    private var mAdapter: AppSelectListAdapter? = null
    private var mAppIconDrawable: Drawable? = null
    private var mAppIconResourceId = 0
    private var mTitle: CharSequence? = null
    var value: String? = null
    private var mPm: PackageManager? = null
    private val mInstalledPackages: MutableList<PackageItem> = LinkedList()

    class PackageItem : Comparable<PackageItem> {
        val mTitle: CharSequence
        val mAppIconResourceId: Int
        val mComponentName: ComponentName?
        val mValue: String

        internal constructor(title: CharSequence, iconResourceId: Int, componentName: ComponentName) {
            mTitle = title
            mAppIconResourceId = iconResourceId
            mComponentName = componentName
            mValue = componentName.flattenToString()
        }

        internal constructor(title: CharSequence, iconResourceId: Int, value: String) {
            mTitle = title
            mAppIconResourceId = iconResourceId
            mComponentName = null
            mValue = value
        }

        override fun compareTo(another: PackageItem): Int {
            return mTitle.toString().toUpperCase().compareTo(another.mTitle.toString().toUpperCase())
        }

        override fun hashCode(): Int {
            return mValue.hashCode()
        }

        override fun equals(another: Any?): Boolean {
            return if (another == null || another !is PackageItem) {
                false
            } else mValue == another.mValue
        }
    }

    inner class AppSelectListAdapter(context: Context?) : BaseAdapter() {
        private val mInflater: LayoutInflater

        override fun getCount(): Int {
            return mInstalledPackages.size
        }

        override fun getItem(position: Int): PackageItem {
            return mInstalledPackages[position]
        }

        override fun getItemId(position: Int): Long {
            return mInstalledPackages[position].hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var convertView: View? = convertView
            val holder: ViewHolder
                convertView = mInflater.inflate(R.layout.applist_preference_icon, null, false)
                holder = ViewHolder()
                convertView.setTag(holder)
                holder.title = convertView.findViewById(R.id.title) as TextView
                holder.icon = convertView.findViewById(R.id.icon) as ImageView
            val applicationInfo = getItem(position)
            holder.title!!.setText(applicationInfo.mTitle)
            if (applicationInfo.mAppIconResourceId != 0) {
                holder.icon!!.setImageResource(applicationInfo.mAppIconResourceId)
            } else {
                val d: Drawable? = resolveAppIcon(applicationInfo)
                holder.icon!!.setImageDrawable(d)
            }
            return convertView
        }

        fun resolveApplication(componentName: ComponentName): PackageItem? {
            for (item in mInstalledPackages) {
                if (item.mComponentName != null && item.mComponentName.equals(componentName)) {
                    return item
                }
            }
            return null
        }

        private inner class ViewHolder {
            var title: TextView? = null
            var summary: TextView? = null
            var icon: ImageView? = null
        }

        init {
            mInflater = LayoutInflater.from(context)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, color: Int) : super(context, null) {
        init()
    }

    fun setPackageList(installedPackages: List<PackageItem>?) {
        mInstalledPackages.clear()
        mInstalledPackages.addAll(installedPackages!!)
        addSpecialApps()
        mAdapter!!.notifyDataSetChanged()
        updatePreferenceViews()
    }

    private fun init() {
        mPm = getContext().getPackageManager()
        setDialogLayoutResource(R.layout.preference_dialog_applist)
        setLayoutResource(R.layout.preference_app_select)
        setNegativeButtonText(android.R.string.cancel)
        setPositiveButtonText(null)
        setDialogTitle(R.string.choose_app)
        setDialogIcon(null)
        mAdapter = AppSelectListAdapter(getContext())
    }

    private fun addSpecialApps() {
        val cameraItem = PackageItem(getContext().getResources().getString(R.string.camera_entry),
                R.drawable.ic_camera, CAMERA_ENTRY)
        mInstalledPackages.add(0, cameraItem)
        val torchItem = PackageItem(getContext().getResources().getString(R.string.torch_entry),
                R.drawable.ic_flashlight, TORCH_ENTRY)
        mInstalledPackages.add(0, torchItem)
        val ambientDisplay = PackageItem(getContext().getResources().getString(R.string.ambient_display_entry),
                R.drawable.ic_ambient_display, AMBIENT_DISPLAY_ENTRY)
        mInstalledPackages.add(0, ambientDisplay)
        val disabledItem = PackageItem(getContext().getResources().getString(R.string.disabled_entry),
                R.drawable.ic_disabled, DISABLED_ENTRY)
        mInstalledPackages.add(0, disabledItem)
    }

    override protected fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        super.onSetInitialValue(restorePersistedValue, defaultValue)
        if (mTitle != null) {
            setSummary(mTitle)
        } else {
            setSummary(getContext().getResources().getString(R.string.not_ready_summary))
        }
        mAppIconResourceId = R.drawable.ic_disabled
        setIcon(mAppIconResourceId)
    }

    override protected fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        val list: ListView = view.findViewById(R.id.applist) as ListView
        list.setAdapter(mAdapter)
        list.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val info = parent.getItemAtPosition(position) as PackageItem
                value = info.mValue
                if (shouldPersist()) {
                    persistString(value)
                }
                mTitle = info.mTitle
                mAppIconDrawable = null
                mAppIconResourceId = 0
                if (info.mComponentName != null) {
                    mAppIconDrawable = resolveAppIcon(info)
                } else {
                    mAppIconResourceId = info.mAppIconResourceId
                }
                updatePreferenceViews()
                callChangeListener(value)
                getDialog().dismiss()
            }
        })
    }

    private fun updatePreferenceViews() {
        var name: String? = null
        if (shouldPersist()) {
            name = getPersistedString(null)
        } else {
            name = value
        }
        mAppIconResourceId = R.drawable.ic_disabled
        if (name != null) {
            mAppIconDrawable = null
            when (name) {
                DISABLED_ENTRY -> {
                    mTitle = getContext().getResources().getString(R.string.disabled_entry)
                    mAppIconResourceId = R.drawable.ic_disabled
                }
                TORCH_ENTRY -> {
                    mTitle = getContext().getResources().getString(R.string.torch_entry)
                    mAppIconResourceId = R.drawable.ic_flashlight
                }
                CAMERA_ENTRY -> {
                    mTitle = getContext().getResources().getString(R.string.camera_entry)
                    mAppIconResourceId = R.drawable.ic_camera
                }
                AMBIENT_DISPLAY_ENTRY -> {
                    mTitle = getContext().getResources().getString(R.string.ambient_display_entry)
                    mAppIconResourceId = R.drawable.ic_ambient_display
                }
                else -> {
                    val componentName: ComponentName = ComponentName.unflattenFromString(name)
                    val item = mAdapter!!.resolveApplication(componentName)
                    if (item != null) {
                        mTitle = item.mTitle
                        mAppIconDrawable = resolveAppIcon(item)
                    } else {
                        mTitle = getContext().getResources().getString(R.string.resolve_failed_summary)
                    }
                }
            }
        } else {
            mTitle = getContext().getResources().getString(R.string.disabled_entry)
            mAppIconResourceId = R.drawable.ic_disabled
        }
        setSummary(mTitle)
        if (mAppIconDrawable != null) {
            setIcon(mAppIconDrawable)
        } else {
            setIcon(mAppIconResourceId)
        }
    }

    private val defaultActivityIcon: Drawable
        private get() = getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon)

    private fun resolveAppIcon(item: PackageItem): Drawable? {
        var appIcon: Drawable? = null
        try {
            appIcon = mPm!!.getActivityIcon(item.mComponentName)
        } catch (e: NameNotFoundException) {
        }
        if (appIcon == null) {
            appIcon = defaultActivityIcon
        }
        return appIcon
    }

  /*  object AppSelectListPreferenceDialogFragment : CustomDialogPreference.CustomPreferenceDialogFragment() {
        fun newInstance(key: String?): CustomDialogPreference.CustomPreferenceDialogFragment {
            return CustomDialogPreference.CustomPreferenceDialogFragment.newInstance(key)
        }
    }
*/
    companion object {
        private const val TAG = "AppSelectListPreference"
        const val TORCH_ENTRY = "torch"
        const val DISABLED_ENTRY = "disabled"
        const val CAMERA_ENTRY = "camera"
        const val MUSIC_PLAY_ENTRY = "music_play"
        const val MUSIC_PREV_ENTRY = "music_prev"
        const val MUSIC_NEXT_ENTRY = "music_next"
        const val WAKE_ENTRY = "wake"
        const val AMBIENT_DISPLAY_ENTRY = "ambient_display"
    }
}