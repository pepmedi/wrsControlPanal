package blog.domain

import kotlinx.serialization.Serializable

@Serializable
data class BlogMaster(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val doctorId: String = ""
)

val dummyBlogs = listOf(
    BlogMaster(
        id = "1",
        title = "Understanding Mental Health",
        description = "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it." +
                "Explore the importance of mental health and ways to improve it.",
        imageUrl = "https://firebasestorage.googleapis.com/v0/b/we-are-spine.firebasestorage.app/o/doctors%2FDr.%20Sheetal%20Mohite.png?alt=media&token=c5355afe-7ea1-4cee-a0f1-4ad6a89b1f54",
        createdAt = "1682000000000",
        updatedAt = "1682000000000",
        doctorId = "doc001"
    ),
    BlogMaster(
        id = "2",
        title = "Benefits of Regular Exercise",
        description = "How daily physical activity enhances overall well-being.",
        imageUrl = "https://example.com/image2.jpg",
        createdAt = "1682100000000",
        updatedAt = "1682100000000",
        doctorId = "doc002"
    ),
    BlogMaster(
        id = "3",
        title = "Healthy Eating Habits",
        description = "A guide to balanced diets and nutritional choices.",
        imageUrl = "https://example.com/image3.jpg",
        createdAt = "1682200000000",
        updatedAt = "1682200000000",
        doctorId = "doc003"
    ),
    BlogMaster(
        id = "4",
        title = "Managing Diabetes",
        description = "Tips and strategies to manage diabetes effectively.",
        imageUrl = "https://example.com/image4.jpg",
        createdAt = "1682300000000",
        updatedAt = "1682300000000",
        doctorId = "doc004"
    ),
    BlogMaster(
        id = "5",
        title = "Importance of Sleep",
        description = "Why quality sleep is vital for health and productivity.",
        imageUrl = "https://example.com/image5.jpg",
        createdAt = "1682400000000",
        updatedAt = "1682400000000",
        doctorId = "doc005"
    ),
    BlogMaster(
        id = "6",
        title = "Heart Health Awareness",
        description = "Learn about cardiovascular wellness and prevention tips.",
        imageUrl = "https://example.com/image6.jpg",
        createdAt = "1682500000000",
        updatedAt = "1682500000000",
        doctorId = "doc006"
    ),
    BlogMaster(
        id = "7",
        title = "Stress Management Techniques",
        description = "Effective ways to handle stress in daily life.",
        imageUrl = "https://example.com/image7.jpg",
        createdAt = "1682600000000",
        updatedAt = "1682600000000",
        doctorId = "doc007"
    ),
    BlogMaster(
        id = "8",
        title = "First Aid Basics",
        description = "Quick response techniques for common injuries and emergencies.",
        imageUrl = "https://example.com/image8.jpg",
        createdAt = "1682700000000",
        updatedAt = "1682700000000",
        doctorId = "doc008"
    ),
    BlogMaster(
        id = "9",
        title = "Vaccination Guide",
        description = "Understanding vaccines and their schedules.",
        imageUrl = "https://example.com/image9.jpg",
        createdAt = "1682800000000",
        updatedAt = "1682800000000",
        doctorId = "doc009"
    ),
    BlogMaster(
        id = "10",
        title = "Child Nutrition Tips",
        description = "Healthy eating habits for kids and young teens.",
        imageUrl = "https://example.com/image10.jpg",
        createdAt = "1682900000000",
        updatedAt = "1682900000000",
        doctorId = "doc010"
    )
)

