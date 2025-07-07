package com.opennotes.feature_node.domain.use_case

import com.opennotes.feature_node.domain.model.Note
import com.opennotes.feature_node.domain.repository.NoteRepository
import com.opennotes.feature_node.domain.util.NoteOrder
import com.opennotes.feature_node.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotes(
    private val repsoitory: NoteRepository
) {
    operator fun invoke(
        noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending)
    ): Flow<List<Note>> {
        return repsoitory.getNotes().map{ notes->
           when(noteOrder.orderType){
               is OrderType.Ascending-> {
                   when (noteOrder) {
                       is NoteOrder.Title -> notes.sortedBy { it.title.lowercase() }
                       is NoteOrder.Date -> notes.sortedBy { it.timestamp }
                       is NoteOrder.Color -> notes.sortedBy { it.color }
                   }
               }

                   is OrderType.Descending->{
                       when(noteOrder) {
                           is NoteOrder.Title-> notes.sortedByDescending{it.title.lowercase()}
                           is NoteOrder.Date-> notes.sortedByDescending{it.timestamp}
                           is NoteOrder.Color-> notes.sortedByDescending{it.color}
                       }
                   }

           }
        }
    }
}