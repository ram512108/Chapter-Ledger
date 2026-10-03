package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Shelf

@Entity(tableName = "shelves")
data class ShelfEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val colorHex: String
) {
    fun toDomain(bookCount: Int = 0): Shelf {
        return Shelf(
            id = id,
            name = name,
            description = description,
            colorHex = colorHex,
            bookCount = bookCount
        )
    }

    companion object {
        fun fromDomain(shelf: Shelf): ShelfEntity {
            return ShelfEntity(
                id = shelf.id,
                name = shelf.name,
                description = shelf.description,
                colorHex = shelf.colorHex
            )
        }
    }
}
