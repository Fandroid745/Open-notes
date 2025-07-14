package com.opennotes.feature_node.domain.use_case

import com.opennotes.feature_node.domain.model.Note
import com.opennotes.feature_node.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return repository.searchNotes(query)
    }
}
