package com.opennotes.feature_node.domain.util



sealed class NoteOrder (val orderType: OrderType){
     class Title(orderType: OrderType): NoteOrder(orderType)
         class Date(orderType: OrderType): NoteOrder(orderType)
    class Color(orderType: OrderType): NoteOrder(orderType)

    fun copy(OrderType: OrderType): NoteOrder {
        return when(this){
            is Title -> Title(OrderType)
            is Date -> Date(OrderType)
            is Color -> Color(OrderType)
        }
    }
     }