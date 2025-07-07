package com.opennotes.feature_node.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.opennotes.ui.theme.BabyBlue
import com.opennotes.ui.theme.LightGreen

import com.opennotes.ui.theme.RedOrange
import com.opennotes.ui.theme.RedPink
import com.opennotes.ui.theme.Violet

@Entity
data class Note(
    val title:String,
    val content:String,
    val timestamp: String,
    val color:Int,
    @PrimaryKey val id:Int?=null
){
    companion object{
        val noteColors=listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }
}

class InvalidNoteException(message:String):Exception(message){

}


