package blog.domain

import core.domain.DataError
import core.domain.AppResult
import java.io.File

interface BlogRepository {
    suspend fun addBlogToDatabase(blog: BlogMaster, imageFile: File): AppResult<Unit, DataError.Remote>
}