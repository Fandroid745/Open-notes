package com.opennotes.feature_node.domain.use_case

import com.opennotes.feature_node.domain.model.Note
import com.opennotes.feature_node.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNodeById(id)
    }
}
