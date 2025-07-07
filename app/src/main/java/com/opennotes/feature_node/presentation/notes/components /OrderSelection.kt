package com.opennotes.feature_node.presentation.notes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opennotes.feature_node.domain.util.NoteOrder
import com.opennotes.feature_node.domain.util.OrderType


@Composable
fun  OrderSection (
    modifier: Modifier =Modifier,
    noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    onOrderChange:(NoteOrder)->Unit
){
    Column(
        modifier=Modifier
    ) {
        Row(
            modifier=Modifier.fillMaxWidth()
        ) {
                DefaultRadioButton(
                    text="Title",
                    selected = noteOrder is NoteOrder.Title,
                    onSelect={onOrderChange(NoteOrder.Title(noteOrder.orderType))}
                )
            Spacer(modifier=Modifier.width(8.dp))


            DefaultRadioButton(
                text="Date",
                selected = noteOrder is NoteOrder.Date,
                onSelect={onOrderChange(NoteOrder.Date(noteOrder.orderType))}
            )
            Spacer(modifier=Modifier.width(8.dp))


            DefaultRadioButton(
                text="Color",
                selected = noteOrder is NoteOrder.Color,
                onSelect={onOrderChange(NoteOrder.Color(noteOrder.orderType))}
            )

        }
        Spacer(modifier=Modifier.height(8.dp))
        Row(
            modifier=Modifier.fillMaxWidth()
        ){
            DefaultRadioButton(
                text="Ascending",
                selected = noteOrder.orderType is OrderType.Ascending,
                onSelect={
                    onOrderChange(noteOrder.copy(OrderType.Ascending))}
            )
            Spacer(modifier=Modifier.width(8.dp))


            DefaultRadioButton(
                text="Descending",
                selected = noteOrder.orderType is OrderType.Descending,
                onSelect={noteOrder.copy(OrderType.Descending)}
            )
        }
    }
}