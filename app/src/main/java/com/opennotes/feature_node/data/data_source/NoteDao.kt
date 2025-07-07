package com.opennotes.feature_node.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.opennotes.feature_node.domain.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(value="SELECT*FROM note")
    fun getNotes():Flow<List<Note>>

    @Query(value="SELECT*FROM note WHERE id=:id")
    suspend fun getNoteById(id:Int): Note?

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}