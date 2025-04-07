package login.domain

import controlPanalUser.domain.UserMasterControlPanel
import core.domain.DataError
import core.domain.Result

interface LoginRepository {
    suspend fun isValidUser(username:String,password:String):Result<UserMasterControlPanel, DataError.Remote>
}