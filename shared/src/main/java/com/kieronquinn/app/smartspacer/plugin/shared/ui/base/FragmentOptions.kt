package com.kieronquinn.app.smartspacer.plugin.shared.ui.base

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.kieronquinn.app.shared.R

interface BackAvailable {
    val backIcon: Int
        get() = R.drawable.ic_back
}

interface LockCollapsed
interface NoToolbar
interface Root

interface CanShowSnackbar {
    fun setSnackbarVisible(visible: Boolean){
        //No-op by default
    }
}

interface ProvidesBack {
    fun onBackPressed(): Boolean
}

interface ProvidesTitle {
    fun getTitle(): CharSequence?
}

interface HideBottomNavigation {
    fun shouldHideBottomNavigation(): Boolean = true
}

interface ProvidesOverflow {
    fun inflateMenu(menuInflater: MenuInflater, menu: Menu)
    fun onMenuItemSelected(menuItem: MenuItem): Boolean
}