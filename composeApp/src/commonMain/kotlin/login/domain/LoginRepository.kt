package login.domain

import controlPanalUser.domain.UserMasterControlPanel
import core.domain.DataError
import core.domain.AppResult

interface LoginRepository {
    suspend fun isValidUser(username:String,password:String):AppResult<UserMasterControlPanel, DataError.Remote>
}