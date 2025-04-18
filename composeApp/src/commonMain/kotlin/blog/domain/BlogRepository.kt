package blog.domain

import core.domain.DataError
import core.domain.Result
import java.io.File

interface BlogRepository {
    suspend fun addBlogToDatabase(blog: BlogMaster, imageFile: File): Result<Unit, DataError.Remote>
}