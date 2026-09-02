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

package com.opennotes.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

private const val HEX_COLOR_LENGTH = 6
private const val RGB_MASK = 0x00FFFFFF

fun Color.toHexRgb(): String = String.format("%06X", RGB_MASK and toArgb())

fun String.toColorOrNull(): Color? {
    val sanitized = removePrefix("#").trim()
    if (sanitized.length != HEX_COLOR_LENGTH) return null
    if (!sanitized.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) return null

    return Color(android.graphics.Color.parseColor("#$sanitized"))
}

fun Color.contrastAgainst(other: Color): Float {
    val l1 = luminance() + 0.05f
    val l2 = other.luminance() + 0.05f
    return if (l1 > l2) l1 / l2 else l2 / l1
}

fun Color.contentColorForBackground(): Color {
    val whiteContrast = contrastAgainst(Color.White)
    val blackContrast = contrastAgainst(Color.Black)
    return if (whiteContrast >= blackContrast) Color.White else Color.Black
}
