package com.example.strata.data.model

import com.example.strata.ui.editor.EditorViewModel
import java.util.UUID

data class Template(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "Popular", "Scenic", "Minimalist"
    val thumbnailUrl: String,
    val backgroundUrl: String,
    val elements: List<EditorViewModel.EditorElement>
)
