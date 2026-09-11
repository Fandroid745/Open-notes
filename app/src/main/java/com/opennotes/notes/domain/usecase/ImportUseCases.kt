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

package com.opennotes.notes.domain.usecase

import com.opennotes.notes.data.repository.FileHandler
import com.opennotes.notes.data.repository.JsonHandler
import com.opennotes.notes.domain.model.Note
import com.opennotes.notes.domain.repository.NoteRepository
import com.opennotes.notes.domain.util.ImportResult
import kotlinx.serialization.json.*

class ImportUseCases(
    private val repository: NoteRepository,
    private val fileHandler: FileHandler,
    private val jsonHandler: JsonHandler,
) {
    suspend operator fun invoke(fileUriString: String): ImportResult {
        return try {
            val notesJson =
                try {
                    fileHandler.importBackupFromZip(fileUriString) ?: throw Exception("notes.json not found in zip")
                } catch (e: Exception) {
                    fileHandler.readTextFromUri(fileUriString)
                }

            if (notesJson.isBlank()) {
                return ImportResult.Error("File is empty")
            }

            // Deserialize with validation - catch invalid JSON first
            val rawNotes =
                try {
                    jsonHandler.parseToJsonArray(notesJson)
                } catch (e: Exception) {
                    return ImportResult.Error("Invalid JSON format: ${e.message}")
                }

            if (rawNotes.isEmpty()) {
                return ImportResult.Error("No notes found in file")
            }

            // Validate and construct trusted Note objects
            val validNotes =
                rawNotes.mapNotNull { element ->
                    val rawNote = element as? JsonObject ?: return@mapNotNull null
                    val title = rawNote["title"]?.jsonPrimitive?.contentOrNull?.trim()
                    val content = rawNote["content"]?.jsonPrimitive?.contentOrNull?.trim()

                    // Support both old and new schema during import
                    val createdAt =
                        rawNote["createdAt"]?.jsonPrimitive?.longOrNull
                            ?: rawNote["timestamp"]?.jsonPrimitive?.longOrNull

                    val updatedAt = rawNote["updatedAt"]?.jsonPrimitive?.longOrNull ?: createdAt
                    val color = rawNote["color"]?.jsonPrimitive?.intOrNull

                    // Only create Note if all required fields are present and valid
                    if ((title?.isNotBlank() == true || content?.isNotBlank() == true) &&
                        createdAt != null &&
                        color != null
                    ) {
                        Note(
                            title = title ?: "",
                            content = content ?: "",
                            createdAt = createdAt,
                            updatedAt = updatedAt ?: createdAt,
                            color = color,
                            id = null,
                        )
                    } else {
                        null // Skip invalid notes
                    }
                }

            if (validNotes.isEmpty()) {
                return ImportResult.Error("No valid notes found ")
            }

            repository.insertNotes(validNotes)

            ImportResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("Failed to import notes: ${e.localizedMessage}")
        }
    }
}
