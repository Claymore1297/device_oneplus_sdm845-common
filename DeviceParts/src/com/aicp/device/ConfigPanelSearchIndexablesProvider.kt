/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aicp.device

import android.database.Cursor
import android.database.MatrixCursor
import android.provider.SearchIndexableResource
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_CLASS_NAME
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_ICON_RESID
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_ACTION
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RANK
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RESID
import android.provider.SearchIndexablesContract.INDEXABLES_RAW_COLUMNS
import android.provider.SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS
import android.provider.SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS
import android.provider.SearchIndexablesProvider

class ConfigPanelSearchIndexablesProvider : SearchIndexablesProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun queryXmlResources(projection: Array<String?>?): Cursor {
        return MatrixCursor(INDEXABLES_XML_RES_COLUMNS)
    }

    override fun queryRawData(projection: Array<String?>?): Cursor {
        return MatrixCursor(INDEXABLES_RAW_COLUMNS)
    }

    override fun queryNonIndexableKeys(projection: Array<String?>?): Cursor {
        return MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS)
    }

    companion object {
        private const val TAG = "ConfigPanelSearchIndexablesProvider"
        const val SEARCH_IDX_BUTTON_PANEL = 0
        const val SEARCH_IDX_GESTURE_PANEL = 1
        const val SEARCH_IDX_OCLICK_PANEL = 2
        private val INDEXABLE_RES: Array<SearchIndexableResource> = arrayOf<SearchIndexableResource>(
                SearchIndexableResource(1, R.xml.main,
                        DeviceSettingsActivity::class.java.name,
                        R.drawable.ic_settings_device))

        private fun generateResourceRef(sir: SearchIndexableResource): Array<Any?> {
            val ref = arrayOfNulls<Any>(7)
            ref[COLUMN_INDEX_XML_RES_RANK] = sir.rank
            ref[COLUMN_INDEX_XML_RES_RESID] = sir.xmlResId
            ref[COLUMN_INDEX_XML_RES_CLASS_NAME] = null
            ref[COLUMN_INDEX_XML_RES_ICON_RESID] = sir.iconResId
            ref[COLUMN_INDEX_XML_RES_INTENT_ACTION] = "com.android.settings.action.EXTRA_SETTINGS"
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE] = "com.aicp.device"
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS] = sir.className
            return ref
        }
    }
}