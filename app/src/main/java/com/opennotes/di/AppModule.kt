package com.opennotes.di

import android.app.Application
import androidx.room.Room
import com.opennotes.feature_node.data.data_source.NoteDatabase
import com.opennotes.feature_node.data.repository.NoteRepositoryImpl
import com.opennotes.feature_node.domain.repository.NoteRepository
import com.opennotes.feature_node.domain.use_case.AddNote
import com.opennotes.feature_node.domain.use_case.DeleteNote
import com.opennotes.feature_node.domain.use_case.GetNote
import com.opennotes.feature_node.domain.use_case.GetNotes
import com.opennotes.feature_node.domain.use_case.NoteUseCases
import com.opennotes.feature_node.domain.use_case.SearchNotesUseCase
import com.opennotes.feature_node.presentation.notes.NotesEvent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDataBase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            NoteDatabase.DATABASE_NAME

        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(db: NoteDatabase): NoteRepository {
        return NoteRepositoryImpl(db.noteDao)
    }

    @Provides
    @Singleton
    fun provideNoteUseCaseId(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            getNotes= GetNotes(repository),
            deleteNote= DeleteNote(repository),
            addNote = AddNote(repository),
            getNote = GetNote(repository),
            searchNotes= SearchNotesUseCase(repository)
        )
    }

}