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

package com.opennotes.notes.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class],
    version = 6,
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao

    companion object {
        const val DATABASE_NAME = "notes_db"

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Note ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Note ADD COLUMN reminderTime INTEGER")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Note ADD COLUMN repeatInterval INTEGER")
                    db.execSQL("ALTER TABLE Note ADD COLUMN repeatUnit TEXT")
                }
            }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `Note_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `color` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, `reminderTime` INTEGER, `repeatInterval` INTEGER, `repeatUnit` TEXT, `id` INTEGER, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `Note_new` (title, content, createdAt, updatedAt, color, isPinned, reminderTime, repeatInterval, repeatUnit, id) SELECT title, content, timestamp, timestamp, color, isPinned, reminderTime, repeatInterval, repeatUnit, id FROM `Note`")
                db.execSQL("DROP TABLE `Note`")
                db.execSQL("ALTER TABLE `Note_new` RENAME TO `Note`")
            }
        }
    }
}
