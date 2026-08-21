
/*
 *
 *  *  Copyright (c) 2026 Dhanush Sugganahalli <dhanush41230@gmail.com>
 *  *
 *  *  This program is free software; you can redistribute it and/or modify it under
 *  *  the terms of the GNU General Public License as published by the Free Software
 *  *  Foundation; either version 3 of the License, or (at your option) any later
 *  *  version.
 *  *
 *  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with
 *  *  this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.opennotes.settings.domain.model

import com.opennotes.notes.domain.model.AppIcon

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class NotesLayout {
    GRID,
    COLUMN,
}

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appIcon: AppIcon = AppIcon.DEFAULT,
    val blackTheme: Boolean = false,
    val biometricLock: Boolean = false,
    val secureScreen: Boolean = false,
    val colorScheme: Long = 0L,
    val dynamicColor: Boolean = true,
    val notesLayout: NotesLayout = NotesLayout.GRID,
    // Legacy fields - keeping for migration compatibility
    @Deprecated("Use themeMode instead") val darkTheme: Boolean = false,
    @Deprecated("Use themeMode instead") val systemTheme: Boolean = true,
    @Deprecated("Use themeMode instead") val lightTheme: Boolean = false,
)
