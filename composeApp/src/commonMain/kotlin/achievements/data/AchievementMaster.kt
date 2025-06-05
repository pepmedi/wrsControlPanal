package achievements.data

data class AchievementMaster(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val createdAt: String,
    val updatedAt: String,
) {
    val isFormValid: Boolean =
        name.isNotBlank() && description.isNotBlank() && imageUrl.isNotBlank()
}
