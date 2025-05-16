package blog.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface BlogRepository {
    suspend fun addBlogToDatabase(
        blog: BlogMaster,
        imageFile: File
    ): AppResult<BlogMaster, DataError.Remote>

    suspend fun getAllBlogs(): Flow<AppResult<List<BlogMaster>, DataError.Remote>>

    suspend fun updateBlog(
        blog: BlogMaster,
        imageFile: File?
    ): Flow<AppResult<BlogMaster, DataError.Remote>>

}