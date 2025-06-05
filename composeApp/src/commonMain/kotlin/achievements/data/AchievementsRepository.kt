package achievements.data

import core.domain.AppResult
import core.domain.DataError
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AchievementsRepository {
    suspend fun getAllAchievements(): Flow<AppResult<List<AchievementMaster>, DataError.Remote>>
    suspend fun addAchievement(
        achievementMaster: AchievementMaster,
        image: File
    ): Flow<AppResult<AchievementMaster, DataError.Remote>>

    suspend fun updateAchievement(
        achievementMaster: AchievementMaster,
        image: File?
    ): Flow<AppResult<AchievementMaster, DataError.Remote>>

    suspend fun deleteAchievement(achievementMaster: AchievementMaster): Flow<AppResult<Unit, DataError.Remote>>

}