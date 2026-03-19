package com.example.strata.data.model

import androidx.compose.ui.graphics.Color

sealed class ActivityTemplate(
    val id: String,
    val title: String,
    val category: String,
    val primaryColor: Color = Color.White,
    val secondaryColor: Color = Color.LightGray
) {
    data object ThreeDee1 : ActivityTemplate("temp_3d1", "Retro 3D", "3D & Perspective", Color(0xFFFF5252), Color(0xFFFFEB3B))
    data object ThreeDee2 : ActivityTemplate("temp_3d2", "Neon Depth", "3D & Perspective", Color(0xFFE040FB), Color(0xFF00E5FF))
    data object Curved1 : ActivityTemplate("temp_curved1", "Cyclo Path", "Curved Text", Color(0xFF4CAF50), Color(0xFF8BC34A))
    data object Curved2 : ActivityTemplate("temp_curved2", "Wave Data", "Curved Text", Color(0xFF2196F3), Color(0xFF03A9F4))
    data object GradientShadow1 : ActivityTemplate("temp_grad1", "Vaporwave Glow", "Gradients", Color(0xFFFF4081), Color(0xFF7C4DFF))
    data object GradientShadow2 : ActivityTemplate("temp_grad2", "Sunset Blur", "Gradients", Color(0xFFFF5722), Color(0xFFFFC107))
    data object MinimalistLines : ActivityTemplate("temp_min1", "Swiss Lines", "Minimalist", Color(0xFFE0E0E0), Color(0xFF9E9E9E))
    data object BoldGeometric : ActivityTemplate("temp_bold", "Block Action", "Geometric", Color(0xFFFFFFFF), Color(0xFFFFEB3B))
    data object VerticalStack : ActivityTemplate("temp_stack", "Stat Card", "Layouts", Color(0xFF3F51B5), Color(0xFFE91E63))
    data object RotationTransform : ActivityTemplate("temp_rot", "X-Treme", "Transforms", Color(0xFFFF9800), Color(0xFFF44336))
    
    class Custom(base: ActivityTemplate, primary: Color, secondary: Color) : ActivityTemplate(base.id, base.title, base.category, primary, secondary)
    companion object {
        val allTemplates = listOf(
            ThreeDee1, ThreeDee2,
            Curved1, Curved2,
            GradientShadow1, GradientShadow2,
            MinimalistLines, BoldGeometric,
            VerticalStack, RotationTransform
        )
    }
}
