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

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.MenuItem

class PanelSettingsActivity : Activity() {
    private var mPanelSettingsFragment: PanelSettings? = null
    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar().setDisplayHomeAsUpEnabled(true)
        val fragment: Fragment = getFragmentManager().findFragmentById(android.R.id.content)
            mPanelSettingsFragment = PanelSettings()
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, mPanelSettingsFragment)
                    .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}