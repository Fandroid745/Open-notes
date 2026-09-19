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

package com.opennotes.notes.domain.util

import java.util.Calendar

object ReminderUtils {
    /**
     * Calculates the next trigger time based on the repetition rule.
     * If the calculated time is in the past, it loops until a future time is found (Catch-up logic).
     */
    fun calculateNextTriggerTime(
        currentTime: Long,
        interval: Long,
        unit: String,
    ): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
        val now = System.currentTimeMillis()

        var safetyBreak = 0
        while (calendar.timeInMillis <= now && safetyBreak < 100) {
            when (unit) {
                "MINUTES" -> calendar.add(Calendar.MINUTE, interval.toInt())
                "HOURS" -> calendar.add(Calendar.HOUR_OF_DAY, interval.toInt())
                "DAYS" -> calendar.add(Calendar.DAY_OF_YEAR, interval.toInt())
                "WEEKS" -> calendar.add(Calendar.WEEK_OF_YEAR, interval.toInt())
                "MONTHS" -> calendar.add(Calendar.MONTH, interval.toInt())
                "YEARS" -> calendar.add(Calendar.YEAR, interval.toInt())
                else -> {
                    calendar.add(Calendar.DAY_OF_YEAR, interval.toInt())
                    break
                }
            }
            safetyBreak++
        }
        return calendar.timeInMillis
    }
}
