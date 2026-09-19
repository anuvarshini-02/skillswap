package com.example.data.model

enum class SkillLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    ALL_LEVELS("All Levels")
}

enum class SessionMode(val displayName: String) {
    ONLINE("Online (Video / Meet)"),
    IN_PERSON("In-Person (Campus)"),
    HYBRID("Online or In-Person")
}

data class SkillCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: Long
) {
    companion object {
        val ALL_CATEGORIES = listOf(
            SkillCategory("all", "All Skills", "Category", 0xFF6366F1),
            SkillCategory("programming", "Programming", "Code", 0xFF4F46E5),
            SkillCategory("web_dev", "Web Development", "Language", 0xFF0EA5E9),
            SkillCategory("mobile_dev", "Mobile Development", "Smartphone", 0xFF10B981),
            SkillCategory("ui_ux", "UI/UX Design", "Palette", 0xFFEC4899),
            SkillCategory("graphic_design", "Graphic Design", "Brush", 0xFF8B5CF6),
            SkillCategory("photography", "Photography", "CameraAlt", 0xFFF59E0B),
            SkillCategory("music", "Music", "MusicNote", 0xFFE11D48),
            SkillCategory("guitar", "Guitar", "Piano", 0xFFD97706),
            SkillCategory("public_speaking", "Public Speaking", "RecordVoiceOver", 0xFF059669),
            SkillCategory("languages", "Languages", "Translate", 0xFF2563EB),
            SkillCategory("ai_ml", "Artificial Intelligence", "Psychology", 0xFF7C3AED),
            SkillCategory("data_science", "Data Science", "Analytics", 0xFF0D9488),
            SkillCategory("fitness", "Fitness", "FitnessCenter", 0xFFDC2626),
            SkillCategory("cooking", "Cooking", "Restaurant", 0xFFEA580C),
            SkillCategory("business", "Business", "TrendingUp", 0xFF3B82F6),
            SkillCategory("other", "Other", "MoreHoriz", 0xFF64748B)
        )
    }
}
