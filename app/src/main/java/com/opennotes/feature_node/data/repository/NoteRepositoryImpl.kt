package com.opennotes.feature_node.data.repository

import com.opennotes.feature_node.data.data_source.NoteDao
import com.opennotes.feature_node.domain.model.Note
import com.opennotes.feature_node.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
private val dao: NoteDao
) : NoteRepository


{
    override fun getNotes(): Flow<List<Note>> {
        return dao.getNotes()
    }

    override suspend fun getNodeById(id: Int): Note? {
        return dao.getNoteById(id)
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
     dao.deleteNote(note)
    }
}