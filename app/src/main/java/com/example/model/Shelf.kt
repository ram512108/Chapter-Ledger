package com.example.model

import java.util.UUID

data class Shelf(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val colorHex: String = "#F59E0B",
    val bookCount: Int = 0
)
